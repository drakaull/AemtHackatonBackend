package org.helha.aemthackatonbackend.application.notes.query.getbyid;

import org.helha.aemthackatonbackend.domain.Folder;

import java.time.LocalDateTime;

public class GetbyIdNoteOutput {
    public long id, sizeBytes, lineCount, wordCount, charCount;
    public String title, content;
    public LocalDateTime createdAt, updatedAt;
    Folder folder;
}
