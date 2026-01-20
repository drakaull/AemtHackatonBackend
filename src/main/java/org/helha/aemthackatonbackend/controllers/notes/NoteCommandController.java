package org.helha.aemthackatonbackend.controllers.notes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteHandler;
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
    
    private final CreateNoteHandler createNoteHandler;
    private final UpdateNoteHandler updateNoteHandler;
    
    @PostMapping("/{folderId}/notes")
    public ResponseEntity<CreateNoteOutput> createNote(
            @PathVariable Long folderId,
            @Valid @RequestBody CreateNoteInput input
    ) {
        // sécuriser que folderId vient de l'URL
        CreateNoteInput fixedInput = new CreateNoteInput(
                input.getTitle(),
                folderId
        );
        
        CreateNoteOutput output = createNoteHandler.handle(fixedInput);
        return ResponseEntity.status(201).body(output);
    }
    
    @PutMapping("/{noteId}")
    public ResponseEntity<Void> updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteInput input) {
        updateNoteHandler.handle(noteId, input);
        return ResponseEntity.noContent().build();
    }
}
