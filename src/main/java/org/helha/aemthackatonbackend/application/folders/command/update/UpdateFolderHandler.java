package org.helha.aemthackatonbackend.application.folders.command.update;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.helha.aemthackatonbackend.domain.utils.UniqueNameResolver;

@Service
@RequiredArgsConstructor
public class UpdateFolderHandler {

    private final IFolderRepository folderRepository;
    private final UniqueNameResolver uniqueNameResolver;
    
    @Transactional
    public void handle(UpdateFolderInput input){
        DbFolder folder = folderRepository.findById(input.getFolderId())
                .orElseThrow(()-> new EntityNotFoundException("Dossier introuvable : " + input.getFolderId()));
        
        folder.setName(uniqueNameResolver.uniqueFolderNameForUpdate(folder.getParentId(), folder.getId(), input.getName()));
        folder.setUpdatedAt(LocalDateTime.now());

        folderRepository.save(folder);
    }
}
