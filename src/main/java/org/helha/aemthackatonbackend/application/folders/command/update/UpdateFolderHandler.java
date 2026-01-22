
package org.helha.aemthackatonbackend.application.folders.command.update;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.helha.aemthackatonbackend.domain.utils.UniqueNameResolver;

/**
 * Application service responsible for handling folder updates.
 * This handler manages folder renaming and timestamp updates.
 */
@Service
@RequiredArgsConstructor
public class UpdateFolderHandler {

    /**
     * Repository used to retrieve and persist folder entities.
     */
    private final IFolderRepository folderRepository;

    /**
     * Utility responsible for resolving unique folder names
     * when updating an existing folder.
     */
    private final UniqueNameResolver uniqueNameResolver;

    /**
     * Handles the update folder use case.
     * The transaction ensures consistency in case of concurrent updates.
     *
     * @param input command object containing update data
     */
    @Transactional
    public void handle(UpdateFolderInput input) {

        // Load the folder or fail fast if it does not exist
        DbFolder folder = folderRepository
                .findById(input.getFolderId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Dossier introuvable : " + input.getFolderId()
                        )
                );

        // Update the folder name while ensuring uniqueness
        // within the same parent directory
        folder.setName(
                uniqueNameResolver.uniqueFolderNameForUpdate(
                        folder.getParentId(),
                        folder.getId(),
                        input.getName()
                )
        );

        // Update the last modification timestamp
        folder.setUpdatedAt(LocalDateTime.now());

        // Persist the updated folder
        folderRepository.save(folder);
    }
}