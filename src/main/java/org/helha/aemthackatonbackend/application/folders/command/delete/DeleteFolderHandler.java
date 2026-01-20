package org.helha.aemthackatonbackend.application.folders.command.delete;


import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFolderHandler {
    
    private final IFolderRepository folderRepository;
    
    public void handle(Long folderId) {
        if (!folderRepository.existsById(folderId)) {
            throw new IllegalArgumentException("Folder not found");
        }
        folderRepository.deleteById(folderId);
    }
}

