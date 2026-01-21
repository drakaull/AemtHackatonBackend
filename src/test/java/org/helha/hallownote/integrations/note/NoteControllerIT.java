package org.helha.hallownote.integrations.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.helha.aemthackatonbackend.AemtHackatonBackendApplication;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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

import static org.hamcrest.Matchers.containsString;
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
public class NoteControllerIT {
    
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
        f.parentId = 0L;
        f.createdAt = LocalDateTime.now();
        f.updatedAt = LocalDateTime.now();
        return folderRepository.save(f);
    }
    
    @Test
    void create_get_metadata_update_export_delete_note_happy_path() throws Exception {
        // Arrange
        DbFolder root = seedRootFolder("Root");
        
        // 1) CREATE note
        String createJson = objectMapper.writeValueAsString(Map.of(
                "title", "My Note",
                "folderId", root.id
        ));
        
        String createResponse = mockMvc.perform(
                        post(CTX + "/notes/{folderId}/notes", root.id)
                                .contextPath(CTX)
                                .contentType("application/json")
                                .content(createJson)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.folderId").value((int) root.id))
                .andExpect(jsonPath("$.title").value("My Note"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        long noteId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // 2) GET note by id
        mockMvc.perform(get(CTX + "/notes/{noteId}", noteId).contextPath(CTX))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value((int) noteId))
                .andExpect(jsonPath("$.title").value("My Note"))
                .andExpect(jsonPath("$.folderId").value((int) root.id));
        
        // 3) GET metadata
        mockMvc.perform(get(CTX + "/notes/{noteId}/metadata", noteId).contextPath(CTX))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value((int) noteId))
                .andExpect(jsonPath("$.title").value("My Note"));
        
        // 4) UPDATE note
        String updateJson = objectMapper.writeValueAsString(Map.of(
                "title", "My Note v2",
                "content", "Hello content"
        ));
        
        mockMvc.perform(
                        put(CTX + "/notes/{noteId}", noteId)
                                .contextPath(CTX)
                                .contentType("application/json")
                                .content(updateJson)
                )
                .andExpect(status().isNoContent());
        
        // 5) EXPORT PDF
        // D'après ton code, le chemin export est sous base "/notes" et contient "/notes/{noteId}/export/pdf"
        // -> donc endpoint complet: /notes/notes/{noteId}/export/pdf
        mockMvc.perform(
                        get(CTX + "/notes/notes/{noteId}/export/pdf", noteId)
                                .contextPath(CTX)
                                .param("includeMetadata", "false")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("note-" + noteId + ".pdf")))
                .andExpect(content().contentType("application/pdf"));
        
        // 6) DELETE note
        mockMvc.perform(delete(CTX + "/notes/{noteId}", noteId).contextPath(CTX))
                .andExpect(status().isNoContent());
    }
}
