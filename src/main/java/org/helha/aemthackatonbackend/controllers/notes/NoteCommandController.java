
package org.helha.aemthackatonbackend.controllers.notes;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.notes.command.NoteCommandProcessor;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteInput;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteOutput;
import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class NoteCommandController {
    
    private final NoteCommandProcessor noteCommandProcessor;
    
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    headers = @Header(
                            name = "location",
                            description = "location od created resource")
            ),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @PostMapping("/{folderId}/notes")
    public ResponseEntity<CreateNoteOutput> createNote(
            @PathVariable Long folderId,
            @Valid @RequestBody CreateNoteInput input
    ) {
        // On force le folderId depuis l'URL
        CreateNoteOutput output = noteCommandProcessor.createNoteHandler.handle(input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id)
                .toUri();
        return ResponseEntity
                .created(location)
                .body(output);
    }
    
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteInput input) {
        try {
            noteCommandProcessor.updateNoteHandler.handle(noteId, input);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        try {
            noteCommandProcessor.deleteNoteHandler.handle(noteId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
