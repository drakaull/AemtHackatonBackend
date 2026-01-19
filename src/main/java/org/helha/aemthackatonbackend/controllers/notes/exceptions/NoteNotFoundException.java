
package org.helha.aemthackatonbackend.controllers.notes.exceptions;

public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(Long id) {
        super("Note with ID " + id + " not found");
    }
}