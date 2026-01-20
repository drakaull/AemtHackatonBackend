
package org.helha.aemthackatonbackend.application.notes.command;

import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteHandler;
import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteHandler;
import org.springframework.stereotype.Service;

@Service
public class NoteCommandProcessor {

    public final CreateNoteHandler createNoteHandler;
    public final UpdateNoteHandler updateNoteHandler;

    public NoteCommandProcessor(CreateNoteHandler createNoteHandler, UpdateNoteHandler updateNoteHandler, UpdateNoteHandler updateNoteHandler1) {
        this.createNoteHandler = createNoteHandler;
        this.updateNoteHandler = updateNoteHandler1;
    }
}
