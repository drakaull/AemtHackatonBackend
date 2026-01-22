package org.helha.aemthackatonbackend.infrastructure.folder;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "folders")
@Data
@NoArgsConstructor
public class DbFolder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    
    @NotBlank
    public String name;
    
    @Column(name = "parent_id")
    public Long parentId;
    
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

