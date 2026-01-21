package org.helha.hallownote.integrations.folder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.helha.aemthackatonbackend.AemtHackatonBackendApplication;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(
        classes = AemtHackatonBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class FolderControllerIT {
    
    private static final String CTX = "/api";
    
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private IFolderRepository folderRepository;
    
    private DbFolder seedRootFolder(String name) {
        DbFolder f = new DbFolder();
        f.name = name;
        f.parentId = 0L; // racine
        f.createdAt = LocalDateTime.now();
        f.updatedAt = LocalDateTime.now();
        return folderRepository.save(f);
    }
    
    @Test
    void create_get_contents_update_delete_folder_happy_path() throws Exception {
        // Arrange
        DbFolder root = seedRootFolder("Root");
        
        // 1) CREATE folder
        String createJson = objectMapper.writeValueAsString(Map.of(
                "name", "Child",
                "parentId", root.id
        ));
        
        String createResponse = mockMvc.perform(
                        post(CTX + "/folders/{parentId}", root.id)
                                .contextPath(CTX)
                                .contentType("application/json")
                                .content(createJson)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Child"))
                .andExpect(jsonPath("$.parentId").value((int) root.id))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        long childId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // 2) GET folder by id
        mockMvc.perform(get(CTX + "/folders/{folderId}", childId).contextPath(CTX))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value((int) childId))
                .andExpect(jsonPath("$.name").value("Child"))
                .andExpect(jsonPath("$.parentId").value((int) root.id));
        
        // 3) GET contents of root
        mockMvc.perform(get(CTX + "/folders/{folderId}/contents", root.id).contextPath(CTX))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.folders", notNullValue()))
                .andExpect(jsonPath("$.folders[*].id", hasItem((int) childId)))
                .andExpect(jsonPath("$.folders[*].name", hasItem("Child")));
        
        // 4) UPDATE folder
        String updateJson = objectMapper.writeValueAsString(Map.of(
                "folderId", childId,
                "name", "Child v2"
        ));
        
        mockMvc.perform(
                        put(CTX + "/folders/{folderId}", childId)
                                .contextPath(CTX)
                                .contentType("application/json")
                                .content(updateJson)
                )
                .andExpect(status().isNoContent());
        
        // 5) GET folder by id => name updated
        mockMvc.perform(get(CTX + "/folders/{folderId}", childId).contextPath(CTX))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.name").value("Child v2"));
        
        // 6) DELETE folder
        mockMvc.perform(delete(CTX + "/folders/{folderId}", childId).contextPath(CTX))
                .andExpect(status().isNoContent());
    }
}
