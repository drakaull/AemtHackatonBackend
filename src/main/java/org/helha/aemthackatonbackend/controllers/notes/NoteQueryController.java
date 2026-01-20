package org.helha.aemthackatonbackend.controllers.notes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteQueryController {
    

    private final GetAllDatasFromNoteHandler getAllDatasFromNoteHandler;
    private final GetByIdNoteHandler getByIdNoteHandler;


    @Operation(summary = "Find all metadatas from a note")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a note is not found",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("/{noteId}/metadata")
    public ResponseEntity<GetAllDatasFromNoteOutput> getNoteMetadata(@PathVariable Long noteId) {
        GetAllDatasFromNoteOutput output = getAllDatasFromNoteHandler.handle(noteId);
        return ResponseEntity.ok(output);
    }
    
    @Operation(summary = "Find a note by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a note is not found",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("/{noteId}")
    public ResponseEntity<GetByIdNoteOutput> getNoteById(@PathVariable Long noteId) {
        GetByIdNoteOutput output = getByIdNoteHandler.handle(noteId);
        return ResponseEntity.ok(output);
    }
}

