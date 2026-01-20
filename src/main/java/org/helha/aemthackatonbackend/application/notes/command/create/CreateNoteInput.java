package org.helha.aemthackatonbackend.application.notes.command.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateNoteInput {

    @NotBlank(message = "Le titre est obligatoire")
    String title;
    @NotNull(message = "L'identifiant est obligatoire")
    @Positive(message = "L'identifiant doit être positif")
    Long folderId;
}
