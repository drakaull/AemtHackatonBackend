
package org.helha.aemthackatonbackend.controllers.folders;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.command.FolderCommandProcessor;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderInput;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderOutput;
import org.helha.aemthackatonbackend.application.folders.command.update.UpdateFolderInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing folder command endpoints.
 * This controller delegates business logic to command processors.
 */
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderCommandController {

    /**
     * Processor coordinating folder-related command handlers.
     */
    private final FolderCommandProcessor folderCommandProcessor;

    /**
     * Creates a new folder under the specified parent folder.
     *
     * @param parentId identifier of the parent folder
     * @param input    request payload containing folder data
     * @return HTTP 201 response with the created folder
     */
    @PostMapping("/{parentId}")
    public ResponseEntity<CreateFolderOutput> createFolder(
            @PathVariable Long parentId,
            @Valid @RequestBody CreateFolderInput input
    ) {

        // Force the parent id from the URL to avoid client-side manipulation
        CreateFolderInput fixedInput = new CreateFolderInput(
                input.getName(),
                parentId
        );

        CreateFolderOutput output =
                folderCommandProcessor.createFolder(fixedInput);

        return ResponseEntity.status(201).body(output);
    }

    /**
     * Deletes a folder by its identifier.
     *
     * @param folderId identifier of the folder to delete
     * @return HTTP 204 response if deletion succeeds
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {

        try {
            folderCommandProcessor.deleteFolderHandler.handle(folderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Exception is rethrown to be handled by global exception handling
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Updates the name of an existing folder.
     *
     * @param folderId identifier of the folder to update
     * @param input    request payload containing the new folder name
     * @return HTTP 204 response when the update is successful
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<Void> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody UpdateFolderInput input
    ) {

        // Ensure the folder id comes from the URL
        UpdateFolderInput fixedInput = new UpdateFolderInput(
                folderId,
                input.getName()
        );

        folderCommandProcessor.updateFolderHandler.handle(fixedInput);
        return ResponseEntity.noContent().build();
    }
}
