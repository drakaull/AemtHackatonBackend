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

@Service
public class CreateNoteHandler {

    private final INoteRepository noteRepository;
    private final IFolderRepository folderRepository;
    private final MetadataCalculator metadataCalculator;

    public CreateNoteHandler(INoteRepository noteRepository,
                             IFolderRepository folderRepository,
                             MetadataCalculator metadataCalculator) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
        this.metadataCalculator = metadataCalculator;
    }

    @Transactional
    public CreateNoteOutput handle(@Valid CreateNoteInput input) {

        // 1) Vérifier que le dossier existe
        if (!folderRepository.existsById(input.folderId)) {
            throw new EntityNotFoundException("Le dossier " + input.folderId + " n'existe pas.");
        }

        // 2) Construire la note
        String content = ""; // contenu vide par défaut

        DbNote entity = new DbNote();
        entity.setFolderId(input.folderId);
        entity.setTitle(input.title.trim());
        entity.setContent(content);

        // 3) Métadonnées
        entity.setSizeBytes(metadataCalculator.computeSizeBytes(content));
        entity.setLineCount((long) metadataCalculator.computeLineCount(content));
        entity.setWordCount((long) metadataCalculator.computeWordCount(content));
        entity.setCharCount((long) metadataCalculator.computeCharCount(content));

        // 4) Dates
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // 5) Save
        DbNote saved = noteRepository.save(entity);

        // 6) Output
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

