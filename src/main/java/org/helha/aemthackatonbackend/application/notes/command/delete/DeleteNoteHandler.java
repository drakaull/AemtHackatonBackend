
package org.helha.aemthackatonbackend.application.notes.command.delete;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteNoteHandler {
    
    private final INoteRepository noteRepository;
    
    public void handle(Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            throw new IllegalArgumentException("Note not found");
        }
        noteRepository.deleteById(noteId);
    }
}