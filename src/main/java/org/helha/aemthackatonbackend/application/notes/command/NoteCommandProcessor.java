
package org.helha.aemthackatonbackend.application.notes.command;

import org.helha.aemthackatonbackend.application.notes.command.create.CreateNoteHandler;
import org.helha.aemthackatonbackend.application.notes.command.delete.DeleteNoteHandler;
import org.helha.aemthackatonbackend.application.notes.command.export.ExportNoteToPdfHandler;
import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteHandler;
import org.springframework.stereotype.Service;

@Service
public class NoteCommandProcessor {
    
    public final CreateNoteHandler createNoteHandler;
    public final UpdateNoteHandler updateNoteHandler;
    public final DeleteNoteHandler deleteNoteHandler;
    public final ExportNoteToPdfHandler exportNoteToPdfHandler;
    
    public NoteCommandProcessor(CreateNoteHandler createNoteHandler, UpdateNoteHandler updateNoteHandler, UpdateNoteHandler updateNoteHandler1, DeleteNoteHandler deleteNoteHandler, ExportNoteToPdfHandler exportNoteToPdfHandler) {
        this.createNoteHandler = createNoteHandler;
        this.updateNoteHandler = updateNoteHandler1;
        this.deleteNoteHandler = deleteNoteHandler;
        this.exportNoteToPdfHandler = exportNoteToPdfHandler;
    }
}
