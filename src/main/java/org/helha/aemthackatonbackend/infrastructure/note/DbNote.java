
package org.helha.aemthackatonbackend.infrastructure.note;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="notes")
@Data
@NoArgsConstructor
public class DbNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long folderId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    private Long sizeBytes;
    private Long lineCount;
    private Long wordCount;
    private Long charCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
