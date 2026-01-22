package org.helha.aemthackatonbackend.application.notes.command.update;

import jakarta.validation.constraints.NotBlank;

public class UpdateNoteInput {
    
    @NotBlank
    public String title;
    
    public String content;
    
}
