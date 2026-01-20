
package org.helha.aemthackatonbackend.application.notes.command;

import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteHandler;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteInput;
import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteOutput;
import org.springframework.stereotype.Service;

@Service
public class NoteCommandProcessor {

    private final CreateNoteHandler createNoteHandler;

    public NoteCommandProcessor(CreateNoteHandler createNoteHandler) {
        this.createNoteHandler = createNoteHandler;
    }

    public CreateNoteOutput create(CreateNoteInput input) {
        return createNoteHandler.handle(input);
    }

}
