package org.helha.aemthackatonbackend.application.notes.query.getbyid;

import org.helha.aemthackatonbackend.domain.Folder;

import java.time.LocalDateTime;

public class GetByIdNoteOutput {
    public long id;
    public String title;
    public String content;
    public long sizeBytes;
    public int lineCount;
    public int wordCount;
    public int charCount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public long folderId;
}
