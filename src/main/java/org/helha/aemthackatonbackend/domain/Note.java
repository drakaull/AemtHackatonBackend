package org.helha.aemthackatonbackend.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class Note {
    private Long id;
    private Long folderId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long sizeBytes;
    private Long lineCount;
    private Long wordCount;
    private Long charCount;
}
