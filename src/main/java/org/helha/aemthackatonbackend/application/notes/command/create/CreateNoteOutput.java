package org.helha.aemthackatonbackend.application.notes.command.create;

import java.time.LocalDateTime;

public class CreateNoteOutput {
    Long id;
    Long folderId;
    String title;
    String content;
    Long sizeBytes;
    Long lineCount;
    Long wordCount;
    Long charCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public CreateNoteOutput(Long id,
                            Long folderId,
                            String title,
                            String content,
                            Long sizeBytes,
                            Long lineCount,
                            Long wordCount,
                            Long charCount,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt)
    {}
}
