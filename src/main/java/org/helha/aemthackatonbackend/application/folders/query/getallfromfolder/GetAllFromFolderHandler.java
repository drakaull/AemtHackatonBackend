
package org.helha.aemthackatonbackend.application.folders.query.getallfromfolder;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query handler responsible for retrieving the direct content of a folder.
 * The result includes child folders and notes belonging to the given folder.
 */
@Service
@RequiredArgsConstructor
public class GetAllFromFolderHandler
        implements IQueryHandler<Long, GetAllFromFolderOutput> {

    /**
     * Repository used to retrieve submenu folders.
     */
    private final IFolderRepository folderRepository;

    /**
     * Repository used to retrieve notes stored in the folder.
     */
    private final INoteRepository noteRepository;

    /**
     * Mapper used to convert entities to lightweight summary DTOs.
     */
    private final ModelMapper modelMapper;

    /**
     * Handles the query to retrieve all direct items in a folder.
     *
     * @param folderId identifier of the parent folder
     * @return output containing both subfolders and notes
     */
    @Override
    public GetAllFromFolderOutput handle(Long folderId) {

        // Retrieve direct child folders
        List<DbFolder> folders =
                folderRepository.findByParentId(folderId);

        // Retrieve notes directly contained in the folder
        List<DbNote> notes =
                noteRepository.findByFolderId(folderId);

        // Build the output DTO
        GetAllFromFolderOutput output = new GetAllFromFolderOutput();

        // Map folder entities to summary output objects
        output.folders = folders.stream()
                .map(f -> modelMapper.map(f, FolderSummaryOutput.class))
                .collect(Collectors.toList());

        // Map note entities to summary output objects
        output.notes = notes.stream()
                .map(n -> modelMapper.map(n, NoteSummaryOutput.class))
                .collect(Collectors.toList());

        return output;
    }
}
