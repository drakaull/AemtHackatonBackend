package org.helha.aemthackatonbackend.application.folders.query.export;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportFolderTreeHandler {
    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;

    public FolderExportNode handle(Long folderId){
        DbFolder root = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Dossier pas trouvé : " + folderId));
        return buildTree(root);
    }

    private FolderExportNode buildTree(DbFolder folder){

        //SousDossier
        List<DbFolder> childrenFolders = folderRepository.findByParentId(folder.getId());
        List<FolderExportNode> childNodes = childrenFolders.stream()
                .map(this::buildTree)
                .toList();

        //notes Directes du dosser
        List<DbNote> notes = noteRepository.findByFolderId(folder.getId());
        List<NoteExportNode> noteNodes = notes.stream()
                .map(n -> new NoteExportNode(n.getId(),n.getTitle(),n.getContent()))
                .toList();
        return new FolderExportNode(
                folder.getId(),
                folder.getName(),
                childNodes,
                noteNodes
        );

    }
}
