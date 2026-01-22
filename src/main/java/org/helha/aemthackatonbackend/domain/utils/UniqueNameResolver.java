package org.helha.aemthackatonbackend.domain.utils;

import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Component;

/**
 * Génère automatiquement un nom unique dans un même dossier, en reproduisant
 * le comportement de l'explorateur Windows : "Nom", puis "Nom (1)", "Nom (2)", etc.
 */
@Component
public class UniqueNameResolver {
    
    private final INoteRepository noteRepository;
    private final IFolderRepository folderRepository;
    
    public UniqueNameResolver(INoteRepository noteRepository, IFolderRepository folderRepository) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
    }
    
    public String uniqueNoteTitle(Long folderId, String desiredTitle) {
        String base = normalizeOrDefault(desiredTitle, "Sans titre");
        if (!noteRepository.existsByFolderIdAndTitleIgnoreCase(folderId, base)) {
            return base;
        }
        return incrementUntilAvailableNote(folderId, base, null);
    }
    
    public String uniqueNoteTitleForUpdate(Long folderId, Long noteId, String desiredTitle) {
        String base = normalizeOrDefault(desiredTitle, "Sans titre");
        if (!noteRepository.existsByFolderIdAndTitleIgnoreCaseAndIdNot(folderId, base, noteId)) {
            return base;
        }
        return incrementUntilAvailableNote(folderId, base, noteId);
    }
    
    public String uniqueFolderName(Long parentId, String desiredName) {
        String base = normalizeOrDefault(desiredName, "Nouveau dossier");
        if (!folderRepository.existsByParentIdAndNameIgnoreCase(parentId, base)) {
            return base;
        }
        return incrementUntilAvailableFolder(parentId, base, null);
    }
    
    public String uniqueFolderNameForUpdate(Long parentId, Long folderId, String desiredName) {
        String base = normalizeOrDefault(desiredName, "Nouveau dossier");
        if (!folderRepository.existsByParentIdAndNameIgnoreCaseAndIdNot(parentId, base, folderId)) {
            return base;
        }
        return incrementUntilAvailableFolder(parentId, base, folderId);
    }
    
    private static String normalizeOrDefault(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? defaultValue : trimmed;
    }
    
    private String incrementUntilAvailableNote(Long folderId, String base, Long excludeId) {
        long i = 1;
        while (true) {
            String candidate = base + " (" + i + ")";
            boolean exists = (excludeId == null)
                    ? noteRepository.existsByFolderIdAndTitleIgnoreCase(folderId, candidate)
                    : noteRepository.existsByFolderIdAndTitleIgnoreCaseAndIdNot(folderId, candidate, excludeId);
            if (!exists) {
                return candidate;
            }
            i++;
        }
    }
    
    private String incrementUntilAvailableFolder(Long parentId, String base, Long excludeId) {
        long i = 1;
        while (true) {
            String candidate = base + " (" + i + ")";
            boolean exists = (excludeId == null)
                    ? folderRepository.existsByParentIdAndNameIgnoreCase(parentId, candidate)
                    : folderRepository.existsByParentIdAndNameIgnoreCaseAndIdNot(parentId, candidate, excludeId);
            if (!exists) {
                return candidate;
            }
            i++;
        }
    }
}