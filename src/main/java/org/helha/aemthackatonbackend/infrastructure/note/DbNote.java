package org.helha.aemthackatonbackend.infrastructure.note;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name="notes")

public class DbNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @NotBlank
    public String title;
    public Long folderId;
    public String content;
    public long sizeBytes;
    public long lineCount;
    public long wordCount;
    public long charCount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
