
package org.helha.aemthackatonbackend.domain.utils;

import org.helha.aemthackatonbackend.infrastructure.folder.IFolderRepository;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Component;

/**
 * Utility component responsible for generating unique names within a scope
 * (folder or note parent).
 *
 * The behavior mimics common file explorers (e.g. Windows):
 * "Name", then "Name (1)", "Name (2)", etc.
 */
@Component
public class UniqueNameResolver {

    private final INoteRepository noteRepository;
    private final IFolderRepository folderRepository;

    /**
     * Constructor used for dependency injection.
     */
    public UniqueNameResolver(
            INoteRepository noteRepository,
            IFolderRepository folderRepository
    ) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
    }

    /**
     * Generates a unique note title inside a folder.
     * If the desired title already exists, an incremental suffix is added.
     */
    public String uniqueNoteTitle(Long folderId, String desiredTitle) {

        String base =
                normalizeOrDefault(desiredTitle, "Sans titre");

        if (!noteRepository
                .existsByFolderIdAndTitleIgnoreCase(folderId, base)
        ) {
            return base;
        }

        return incrementUntilAvailableNote(folderId, base, null);
    }

    /**
     * Generates a unique note title when updating an existing note.
     * The current note identifier is excluded from uniqueness checks.
     */
    public String uniqueNoteTitleForUpdate(
            Long folderId,
            Long noteId,
            String desiredTitle
    ) {

        String base =
                normalizeOrDefault(desiredTitle, "Sans titre");

        if (!noteRepository
                .existsByFolderIdAndTitleIgnoreCaseAndIdNot(
                        folderId,
                        base,
                        noteId
                )
        ) {
            return base;
        }

        return incrementUntilAvailableNote(folderId, base, noteId);
    }

    /**
     * Generates a unique folder name inside a parent folder.
     */
    public String uniqueFolderName(Long parentId, String desiredName) {

        String base =
                normalizeOrDefault(desiredName, "Nouveau dossier");

        if (!folderRepository
                .existsByParentIdAndNameIgnoreCase(parentId, base)
        ) {
            return base;
        }

        return incrementUntilAvailableFolder(parentId, base, null);
    }

    /**
     * Generates a unique folder name when updating an existing folder.
     * The current folder identifier is excluded from uniqueness checks.
     */
    public String uniqueFolderNameForUpdate(
            Long parentId,
            Long folderId,
            String desiredName
    ) {

        String base =
                normalizeOrDefault(desiredName, "Nouveau dossier");

        if (!folderRepository
                .existsByParentIdAndNameIgnoreCaseAndIdNot(
                        parentId,
                        base,
                        folderId
                )
        ) {
            return base;
        }

        return incrementUntilAvailableFolder(parentId, base, folderId);
    }

    /**
     * Normalizes input text or falls back to a default value
     * when the provided value is null or blank.
     */
    private static String normalizeOrDefault(
            String value,
            String defaultValue
    ) {

        if (value == null) {
            return defaultValue;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? defaultValue : trimmed;
    }

    /**
     * Increments the note title until an available one is found.
     * Optionally excludes a given note identifier.
     */
    private String incrementUntilAvailableNote(
            Long folderId,
            String base,
            Long excludeId
    ) {

        long i = 1;
        while (true) {

            String candidate = base + " (" + i + ")";

            boolean exists = (excludeId == null)
                    ? noteRepository
                    .existsByFolderIdAndTitleIgnoreCase(
                            folderId,
                            candidate
                    )
                    : noteRepository
                    .existsByFolderIdAndTitleIgnoreCaseAndIdNot(
                            folderId,
                            candidate,
                            excludeId
                    );

            if (!exists) {
                return candidate;
            }

            i++;
        }
    }

    /**
     * Increments the folder name until an available one is found.
     * Optionally excludes a given folder identifier.
     */
    private String incrementUntilAvailableFolder(
            Long parentId,
            String base,
            Long excludeId
    ) {

        long i = 1;
        while (true) {

            String candidate = base + " (" + i + ")";

            boolean exists = (excludeId == null)
                    ? folderRepository
                    .existsByParentIdAndNameIgnoreCase(
                            parentId,
                            candidate
                    )
                    : folderRepository
                    .existsByParentIdAndNameIgnoreCaseAndIdNot(
                            parentId,
                            candidate,
                            excludeId
                    );

            if (!exists) {
                return candidate;
            }

            i++;
        }
    }
}
