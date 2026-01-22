
package org.helha.aemthackatonbackend.application.notes.command.create;

import org.helha.aemthackatonbackend.domain.utils.MetadataCalculator;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.helha.aemthackatonbackend.domain.utils.UniqueNameResolver;

/**
 * Application service responsible for creating notes.
 * This handler initializes metadata, validates folder existence
 * and ensures title uniqueness within a folder.
 */
@Service
public class CreateNoteHandler {

    private final INoteRepository noteRepository;
    private final IFolderRepository folderRepository;
    private final MetadataCalculator metadataCalculator;
    private final UniqueNameResolver uniqueNameResolver;

    /**
     * Explicit constructor used for dependency injection.
     */
    public CreateNoteHandler(
            INoteRepository noteRepository,
            IFolderRepository folderRepository,
            MetadataCalculator metadataCalculator,
            UniqueNameResolver uniqueNameResolver
    ) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
        this.metadataCalculator = metadataCalculator;
        this.uniqueNameResolver = uniqueNameResolver;
    }

    /**
     * Handles the create note use case.
     * The operation is transactional to guarantee consistency.
     *
     * @param input command object containing note creation data
     * @return output DTO representing the created note
     */
    @Transactional
    public CreateNoteOutput handle(@Valid CreateNoteInput input) {

        // Ensure the target folder exists before creating the note
        if (!folderRepository.existsById(input.getFolderId())) {
            throw new EntityNotFoundException(
                    "Le dossier " + input.getFolderId() + " n'existe pas."
            );
        }

        // Initialize note content as empty by default
        String content = "";

        // Create and populate the note entity
        DbNote entity = new DbNote();
        entity.setFolderId(input.getFolderId());

        // Resolve a unique title within the folder
        entity.setTitle(
                uniqueNameResolver.uniqueNoteTitle(
                        input.getFolderId(),
                        input.getTitle()
                )
        );

        entity.setContent(content);

        // Compute metadata based on the initial content
        entity.setSizeBytes(
                metadataCalculator.computeSizeBytes(content)
        );
        entity.setLineCount(
                (long) metadataCalculator.computeLineCount(content)
        );
        entity.setWordCount(
                (long) metadataCalculator.computeWordCount(content)
        );
        entity.setCharCount(
                (long) metadataCalculator.computeCharCount(content)
        );

        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Persist the note
        DbNote saved = noteRepository.save(entity);

        // Map the persisted entity to an output DTO
        return new CreateNoteOutput(
                saved.getId(),
                saved.getFolderId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getSizeBytes(),
                saved.getLineCount(),
                saved.getWordCount(),
                saved.getCharCount(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
