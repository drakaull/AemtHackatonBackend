package org.helha.aemthackatonbackend.application.notes.query.getbyid;

import org.helha.aemthackatonbackend.application.utils.IQueryHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetByIdNoteHandler implements IQueryHandler<Long, GetbyIdNoteOutput> {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;
    
    public GetByIdNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
    }
    
    @Override
    public GetbyIdNoteOutput handle(Long input) {
        Optional<DbNote> entity = noteRepository.findById(input);
        
        if (entity.isPresent())
            return modelMapper.map(entity.get(), GetbyIdNoteOutput.class);
        
        throw new IllegalArgumentException("Note not found");
    }
}
