
package org.helha.aemthackatonbackend.application.notes.command.update;

import org.helha.aemthackatonbackend.domain.utils.MetadataCalculator;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.helha.aemthackatonbackend.domain.utils.UniqueNameResolver;

/**
 * Application service responsible for updating an existing note.
 * This handler manages title updates, content changes and metadata recalculation.
 */
@Service
public class UpdateNoteHandler {

    private final INoteRepository noteRepository;
    private final UniqueNameResolver uniqueNameResolver;

    /**
     * Explicit constructor used for dependency injection.
     */
    public UpdateNoteHandler(
            INoteRepository noteRepository,
            UniqueNameResolver uniqueNameResolver
    ) {
        this.noteRepository = noteRepository;
        this.uniqueNameResolver = uniqueNameResolver;
    }

    /**
     * Handles the update note use case.
     *
     * @param noteId identifier of the note to update
     * @param input  object containing updated title and content
     */
    public void handle(Long noteId, UpdateNoteInput input) {

        // Load the note or fail if it does not exist
        DbNote note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Note not found")
                );

        // Update title and content while ensuring title uniqueness
        note.setTitle(
                uniqueNameResolver.uniqueNoteTitleForUpdate(
                        note.getFolderId(),
                        note.getId(),
                        input.title
                )
        );
        note.setContent(input.content);

        // Recalculate all metadata based on the new content
        note.setSizeBytes(
                MetadataCalculator.computeSizeBytes(note.getContent())
        );
        note.setLineCount(
                (long) MetadataCalculator.computeLineCount(note.getContent())
        );
        note.setWordCount(
                (long) MetadataCalculator.computeWordCount(note.getContent())
        );
        note.setCharCount(
                (long) MetadataCalculator.computeCharCount(note.getContent())
        );

        // Update the last modification timestamp
        note.setUpdatedAt(LocalDateTime.now());

        // Persist the updated note
        noteRepository.save(note);
    }
}
