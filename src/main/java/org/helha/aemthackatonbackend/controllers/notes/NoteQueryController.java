package org.helha.aemthackatonbackend.controllers.notes;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote.GetAllDatasFromNoteHandler;
import org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote.GetAllDatasFromNoteOutput;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderHandler;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderOutput;
import org.helha.aemthackatonbackend.application.notes.query.getbyid.GetByIdNoteHandler;
import org.helha.aemthackatonbackend.application.notes.query.getbyid.GetByIdNoteOutput;
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
    private final GetByIdNoteHandler getByIdNoteHandler;
    
    @Operation(summary = "Find all notes from a folder")
    @GetMapping("/{folderId}/notes")
    public ResponseEntity<GetAllNotesFromFolderOutput> getNotesByFolder(@PathVariable Long folderId) {
        GetAllNotesFromFolderOutput output = getAllNotesFromFolderHandler.handle(folderId);
        return ResponseEntity.ok(output);
    }
    
    @Operation(summary = "Find all metadatas from a note")
    @GetMapping("/notes/{noteId}/metadata")
    public ResponseEntity<GetAllDatasFromNoteOutput> getNoteMetadata(@PathVariable Long noteId) {
        GetAllDatasFromNoteOutput output = getAllDatasFromNoteHandler.handle(noteId);
        return ResponseEntity.ok(output);
    }
    
    @Operation(summary = "Find a note by its ID")
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<GetByIdNoteOutput> getNoteById(@PathVariable Long noteId) {
        GetByIdNoteOutput output = getByIdNoteHandler.handle(noteId);
        return ResponseEntity.ok(output);
    }
}

