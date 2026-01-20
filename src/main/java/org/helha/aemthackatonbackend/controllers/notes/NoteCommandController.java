
package org.helha.aemthackatonbackend.controllers.notes;

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

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteInput input) {
        try {
            noteCommandProcessor.updateNoteHandler.handle(noteId, input);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
