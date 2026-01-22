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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * REST controller responsible for write operations (commands) on notes.
 * This controller handles creation, update, deletion and export of notes.
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteCommandController {

    /**
     * Central entry point for note command handlers.
     * Each handler encapsulates a specific write use case.
     */
    private final NoteCommandProcessor noteCommandProcessor;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    headers = @Header(
                            name = "location",
                            description = "Location of the created resource")
            ),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @PostMapping("/{folderId}/notes")
    public ResponseEntity<CreateNoteOutput> createNote(
            @PathVariable Long folderId,
            @Valid @RequestBody CreateNoteInput input
    ) {
        // The note creation logic is delegated to the command handler.
        // The folderId comes from the URL and is expected to be validated at application level.
        CreateNoteOutput output = noteCommandProcessor.createNoteHandler.handle(input);

        // Build the URI of the newly created note resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id)
                .toUri();

        // Return 201 Created with Location header and response body
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
    public ResponseEntity<Void> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteInput input
    ) {
        try {
            // Delegate update logic to the corresponding command handler
            noteCommandProcessor.updateNoteHandler.handle(noteId, input);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Let the global exception handler translate this into a proper HTTP error
            throw new IllegalArgumentException(e);
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        try {
            // Delete the note identified by its ID
            noteCommandProcessor.deleteNoteHandler.handle(noteId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Error is propagated to be handled by the exception layer
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Exports a note as a PDF file.
     * An optional flag allows including additional metadata in the document.
     */
    @GetMapping("/notes/{noteId}/export/pdf")
    public ResponseEntity<byte[]> exportNoteToPdf(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "false") boolean includeMetadata
    ) throws IOException {

        // Generate the PDF as a byte array using the dedicated handler
        byte[] pdfBytes = noteCommandProcessor.exportNoteToPdfHandler.handle(noteId, includeMetadata);

        // Return the PDF as a downloadable file
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=note-" + noteId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
