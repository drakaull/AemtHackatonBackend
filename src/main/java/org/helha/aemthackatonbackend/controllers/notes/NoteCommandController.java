
package org.helha.aemthackatonbackend.controllers.notes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.notes.command.NoteCommandProcessor;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteInput;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteOutput;
import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteHandler;
import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteInput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class NoteCommandController {

    private final NoteCommandProcessor processor;

    @PostMapping("/{folderId}/notes")
    public ResponseEntity<CreateNoteOutput> createNote(
            @PathVariable Long folderId,
            @Valid @RequestBody CreateNoteInput input
    ) {
        // On force le folderId depuis l'URL
        CreateNoteInput fixedInput = new CreateNoteInput(input.getTitle(), folderId);
        CreateNoteOutput output = processor.create(fixedInput);
        return ResponseEntity.status(201).body(output);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteInput input) {
        updateNoteHandler.handle(noteId, input);
        return ResponseEntity.noContent().build();
    }
}
