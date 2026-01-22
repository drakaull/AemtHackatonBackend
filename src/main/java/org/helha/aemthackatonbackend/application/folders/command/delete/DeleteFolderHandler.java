
package org.helha.aemthackatonbackend.application.folders.command.delete;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.springframework.stereotype.Service;

/**
 * Application service responsible for handling the deletion of folders.
 * This handler ensures the folder exists before performing the delete operation.
 */
@Service
@RequiredArgsConstructor
public class DeleteFolderHandler {

    /**
     * Repository used to access and delete folder entities.
     */
    private final IFolderRepository folderRepository;

    /**
     * Handles the delete folder use case.
     *
     * @param folderId identifier of the folder to delete
     * @throws IllegalArgumentException if the folder does not exist
     */
    public void handle(Long folderId) {

        // Ensure the folder exists before attempting deletion
        if (!folderRepository.existsById(folderId)) {
            throw new IllegalArgumentException("Folder not found");
        }

        // Delete the folder by its identifier
        folderRepository.deleteById(folderId);
    }
}
