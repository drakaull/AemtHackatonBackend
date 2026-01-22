
package org.helha.aemthackatonbackend.application.folders.query.getfolderbyid;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Query handler responsible for retrieving a folder by its identifier.
 * This handler maps the persistence entity to an output DTO.
 */
@Service
@RequiredArgsConstructor
public class GetFolderByIdHandler implements IQueryHandler<Long, GetFolderByIdOutput> {

    /**
     * Repository used to access folder data.
     */
    private final IFolderRepository folderRepository;

    /**
     * Mapper used to convert entities to output DTOs.
     */
    private final ModelMapper modelMapper;

    /**
     * Handles the query to retrieve a folder by its id.
     *
     * @param folderId identifier of the folder
     * @return output DTO representing the folder
     */
    @Override
    public GetFolderByIdOutput handle(Long folderId) {

        // Load the folder or throw an error if it does not exist
        DbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Folder not found")
                );

        // Map the entity to a read model
        return modelMapper.map(folder, GetFolderByIdOutput.class);
    }
}
