
package org.helha.aemthackatonbackend.application.notes.command.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateNoteInput {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotNull(message = "L'identifiant du dossier est obligatoire")
    @Positive(message = "L'identifiant doit être positif")
    private Long folderId;

    public CreateNoteInput(String title, Long folderId) {
        this.title = title;
        this.folderId = folderId;
    }

    public CreateNoteInput() {} // nécessaire pour @RequestBody
}
