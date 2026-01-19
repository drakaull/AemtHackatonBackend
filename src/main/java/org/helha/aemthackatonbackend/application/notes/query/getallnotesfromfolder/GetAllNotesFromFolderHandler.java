
package org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllNotesFromFolderHandler implements IQueryHandler<Long, GetAllNotesFromFolderOutput> {
    
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public GetAllNotesFromFolderOutput handle(Long folderId) {
        List<DbNote> notes = noteRepository.findByFolderId(folderId);
        
        List<NoteSummaryOutput> noteSummaries = notes.stream()
                .map(note -> modelMapper.map(note, NoteSummaryOutput.class))
                .collect(Collectors.toList());
        
        GetAllNotesFromFolderOutput output = new GetAllNotesFromFolderOutput();
        output.notes = noteSummaries;
        return output;
    }
}
