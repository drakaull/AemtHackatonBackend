package org.helha.aemthackatonbackend.controllers.notes;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote.GetAllDatasFromNoteHandler;
import org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote.GetAllDatasFromNoteOutput;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderHandler;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class NoteQueryController {
    
    private final GetAllNotesFromFolderHandler getAllNotesFromFolderHandler;
    private final GetAllDatasFromNoteHandler getAllDatasFromNoteHandler;
    
    @GetMapping("/{folderId}/notes")
    public ResponseEntity<GetAllNotesFromFolderOutput> getNotesByFolder(@PathVariable Long folderId) {
        GetAllNotesFromFolderOutput output = getAllNotesFromFolderHandler.handle(folderId);
        return ResponseEntity.ok(output);
    }
    
    
    @GetMapping("/notes/{noteId}/metadata")
    public ResponseEntity<GetAllDatasFromNoteOutput> getNoteMetadata(@PathVariable Long noteId) {
        GetAllDatasFromNoteOutput output = getAllDatasFromNoteHandler.handle(noteId);
        return ResponseEntity.ok(output);
    }
}

