package org.helha.aemthackatonbackend.application.folders.command.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateFolderInput {

    @NotBlank(message = "Le nom du dossier est obligatoire")
    private String name;

    @NotNull(message = "Le dossier parent est obligatoire")
    @Positive(message = "L'ID du dossier parent doit être positif")
    private Long parentId;


    public CreateFolderInput(String name, Long parentId){
        this.name = name;
        this.parentId = parentId;
    }
}
