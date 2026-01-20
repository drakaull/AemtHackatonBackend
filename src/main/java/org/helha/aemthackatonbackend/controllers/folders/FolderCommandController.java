package org.helha.aemthackatonbackend.controllers.folders;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.command.FolderCommandProcessor;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderInput;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderOutput;
import org.helha.aemthackatonbackend.application.folders.command.update.UpdateFolderInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderCommandController {

    private final FolderCommandProcessor folderCommandProcessor;

    @PostMapping("/{parentId}")
    public ResponseEntity<CreateFolderOutput> createFolder(
            @PathVariable Long parentId,
            @Valid @RequestBody CreateFolderInput input
    ) {
        CreateFolderInput fixedInput = new CreateFolderInput(
                input.getName(),
                parentId
        );
        CreateFolderOutput output = folderCommandProcessor.createFolder(fixedInput);
        return ResponseEntity.status(201).body(output);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        try {
            folderCommandProcessor.deleteFolderHandler.handle(folderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<Void> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody UpdateFolderInput input
    ) {
        UpdateFolderInput fixedInput = new UpdateFolderInput(
                folderId,
                input.getName()
        );
        folderCommandProcessor.updateFolderHandler.handle(fixedInput);
        return ResponseEntity.noContent().build();
    }
}
