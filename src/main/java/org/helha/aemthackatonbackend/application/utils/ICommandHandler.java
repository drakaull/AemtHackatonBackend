package org.helha.aemthackatonbackend.application.utils;

import org.helha.aemthackatonbackend.application.notes.command.update.UpdateNoteInput;

public interface ICommandHandler<I, O> {
    O handle(I input);
    
    void handle(Long noteId, UpdateNoteInput command);
}

