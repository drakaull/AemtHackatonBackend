
package org.helha.aemthackatonbackend.application.folders.query.export;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service responsible for exporting a folder tree structure.
 * The export includes subfolders recursively and all notes contained in each folder.
 */
@Service
@RequiredArgsConstructor
public class ExportFolderTreeHandler {

    /**
     * Repository used to access folder data.
     */
    private final IFolderRepository folderRepository;

    /**
     * Repository used to access notes associated with folders.
     */
    private final INoteRepository noteRepository;

    /**
     * Entry point for exporting a folder tree starting from a root folder.
     *
     * @param folderId identifier of the root folder
     * @return a tree structure representing folders and notes
     */
    public FolderExportNode handle(Long folderId) {

        // Load the root folder or fail if it does not exist
        DbFolder root = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Dossier pas trouvÁ© : " + folderId)
                );

        // Build the full folder tree recursively
        return buildTree(root);
    }

    /**
     * Recursively builds the export tree for a given folder.
     *
     * @param folder current folder entity
     * @return export node representing the folder, its subfolders and notes
     */
    private FolderExportNode buildTree(DbFolder folder) {

        // Retrieve direct child folders
        List<DbFolder> childrenFolders =
                folderRepository.findByParentId(folder.getId());

        // Recursively map child folders to export nodes
        List<FolderExportNode> childNodes = childrenFolders.stream()
                .map(this::buildTree)
                .toList();

        // Retrieve notes directly associated with the current folder
        List<DbNote> notes =
                noteRepository.findByFolderId(folder.getId());

        // Map note entities to export nodes
        List<NoteExportNode> noteNodes = notes.stream()
                .map(n ->
                        new NoteExportNode(
                                n.getId(),
                                n.getTitle(),
                                n.getContent()
                        )
                )
                .toList();

        // Build and return the export node for the current folder
        return new FolderExportNode(
                folder.getId(),
                folder.getName(),
                childNodes,
                noteNodes
        );
    }
}
