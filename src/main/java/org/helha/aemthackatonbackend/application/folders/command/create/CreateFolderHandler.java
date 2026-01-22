
package org.helha.aemthackatonbackend.application.folders.command.create;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.helha.aemthackatonbackend.domain.utils.UniqueNameResolver;

/**
 * Application service responsible for handling the creation of folders.
 * This class orchestrates validation, entity creation and persistence.
 */
@Service
@RequiredArgsConstructor
public class CreateFolderHandler {

    /**
     * Repository used to access and persist folder entities.
     */
    private final IFolderRepository folderRepository;

    /**
     * Utility responsible for resolving unique folder names
     * within the same parent folder.
     */
    private final UniqueNameResolver uniqueNameResolver;

    /**
     * Handles the create folder use case.
     * The operation is transactional to ensure data consistency.
     *
     * @param input command object containing the folder creation data
     * @return output DTO containing the created folder information
     */
    @Transactional
    public CreateFolderOutput handle(CreateFolderInput input) {

        // Validate that the parent folder exists before creating a child folder
        if (!folderRepository.existsById(input.getParentId())) {
            throw new EntityNotFoundException(
                    "Dossier parent introuvable" + input.getParentId()
            );
        }

        // Create a new folder database entity
        DbFolder entity = new DbFolder();

        // Resolve a unique name to avoid duplicates under the same parent
        entity.setName(
                uniqueNameResolver.uniqueFolderName(
                        input.getParentId(),
                        input.getName()
                )
        );

        // Set parent folder reference
        entity.setParentId(input.getParentId());

        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Persist the new folder
        DbFolder saved = folderRepository.save(entity);

        // Map the saved entity to an output DTO
        return new CreateFolderOutput(
                saved.getId(),
                saved.getName(),
                saved.getParentId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
