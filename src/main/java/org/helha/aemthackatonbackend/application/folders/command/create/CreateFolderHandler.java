package org.helha.aemthackatonbackend.application.folders.command.create;

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
public class CreateFolderHandler {

    private final IFolderRepository folderRepository;
    private final UniqueNameResolver uniqueNameResolver;
    
    @Transactional
    public CreateFolderOutput handle(CreateFolderInput input){
        if(!folderRepository.existsById(input.getParentId())){
            throw new EntityNotFoundException("Dossier parent introuvable" + input.getParentId());
        }

        DbFolder entity = new DbFolder();
        entity.setName(uniqueNameResolver.uniqueFolderName(input.getParentId(), input.getName()));
        entity.setParentId(input.getParentId());

        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        DbFolder saved = folderRepository.save(entity);

        return new CreateFolderOutput(
                saved.getId(),
                saved.getName(),
                saved.getParentId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
