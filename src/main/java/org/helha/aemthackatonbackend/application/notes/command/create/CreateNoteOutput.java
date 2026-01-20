package org.helha.aemthackatonbackend.application.notes.command.create;

import java.time.LocalDateTime;

public class CreateNoteOutput {
    public Long id;
    public Long folderId;
    public String title;
    public String content;
    public Long sizeBytes;
    public Long lineCount;
    public Long wordCount;
    public Long charCount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

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
