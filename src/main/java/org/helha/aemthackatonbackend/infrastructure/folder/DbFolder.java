package org.helha.aemthackatonbackend.infrastructure.folder;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;


import java.time.LocalDateTime;

@Entity
@Table(name = "folders")

public class DbFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @NotBlank
    public String name;
    public long parentId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

}
