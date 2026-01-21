package org.helha.aemthackatonbackend.application.folders.query.getfolderbyid;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.helha.aemthackatonbackend.infrastructure.folder.DbFolder;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFolderByIdHandler implements IQueryHandler<Long, GetFolderByIdOutput> {
    private final IFolderRepository folderRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public GetFolderByIdOutput handle(Long folderId) {
        DbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        
        return modelMapper.map(folder, GetFolderByIdOutput.class);
    }
}
