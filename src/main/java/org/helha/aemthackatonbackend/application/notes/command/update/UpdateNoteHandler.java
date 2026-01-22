
package org.helha.aemthackatonbackend.application.notes.command.update;

import org.helha.aemthackatonbackend.domain.utils.MetadataCalculator;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateNoteHandler {
    
    private final INoteRepository noteRepository;
    
    public UpdateNoteHandler(INoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }
    
    public void handle(Long noteId, UpdateNoteInput input) {
        DbNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        // Mettre à jour le titre et le contenu
        note.setTitle(input.title);
        note.setContent(input.content);
        
        // Recalculer les métadonnées
        note.setSizeBytes(MetadataCalculator.computeSizeBytes(note.getContent()));
        note.setLineCount((long) MetadataCalculator.computeLineCount(note.getContent()));
        note.setWordCount((long) MetadataCalculator.computeWordCount(note.getContent()));
        note.setCharCount((long) MetadataCalculator.computeCharCount(note.getContent()));
        
        // Mettre à jour la date de modification
        note.setUpdatedAt(LocalDateTime.now());
        
        // Sauvegarder
        noteRepository.save(note);
    }
}
