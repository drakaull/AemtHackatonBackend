package org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllDatasFromNoteHandler implements IQueryHandler<Long, GetAllDatasFromNoteOutput> {
    
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public GetAllDatasFromNoteOutput handle(Long noteId) {
        DbNote entity = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        return modelMapper.map(entity, GetAllDatasFromNoteOutput.class);
    }
}
