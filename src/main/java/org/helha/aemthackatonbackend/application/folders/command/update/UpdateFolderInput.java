package org.helha.aemthackatonbackend.application.folders.command.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateFolderInput {
    @NotNull(message = "L'ID du dossier est obligatoire")
    private Long folderId;

    @NotBlank(message = "Nom du dossier obligatoire")
    private String name;

    public UpdateFolderInput(Long folderId, String name){
        this.folderId = folderId;
        this.name = name;
    }
}
