package org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote;

import java.time.LocalDateTime;

public class GetAllDatasFromNoteOutput {
    public long id;
    public String title;
    public long sizeBytes;
    public int lineCount;
    public int wordCount;
    public int charCount;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
