package org.helha.aemthackatonbackend.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class Note {
    private long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long sizeBytes;
    private long lineCount;
    private long wordCount;
    private long charCount;
    Folder folder;

}
