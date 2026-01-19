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

@Service
@RequiredArgsConstructor
public class GetAllFromFolderHandler implements IQueryHandler<Long, GetAllFromFolderOutput> {
    
    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public GetAllFromFolderOutput handle(Long folderId) {
        List<DbFolder> folders = folderRepository.findByParentId(folderId);
        List<DbNote> notes = noteRepository.findByFolderId(folderId);
        
        GetAllFromFolderOutput output = new GetAllFromFolderOutput();
        output.folders = folders.stream()
                .map(f -> modelMapper.map(f, FolderSummaryOutput.class))
                .collect(Collectors.toList());
        output.notes = notes.stream()
                .map(n -> modelMapper.map(n, NoteSummaryOutput.class))
                .collect(Collectors.toList());
        
        return output;
    }
}
