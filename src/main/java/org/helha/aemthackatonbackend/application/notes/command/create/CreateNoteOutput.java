package org.helha.aemthackatonbackend.application.notes.command.create;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

}
