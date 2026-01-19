package org.helha.aemthackatonbackend.application.notes.query;

import org.helha.aemthackatonbackend.application.notes.query.getalldatasfromnote.GetAllDatasFromNoteHandler;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderHandler;
import org.helha.aemthackatonbackend.application.notes.query.getbyid.GetByIdNoteHandler;
import org.springframework.stereotype.Service;

@Service
public class NoteQueryProcessor {
    public final GetAllDatasFromNoteHandler getAllDatasFromNoteHandler;
    public final GetAllNotesFromFolderHandler getAllNotesFromFolderHandler;
    public final GetByIdNoteHandler getByIdNoteHandler;
    
    public NoteQueryProcessor(GetAllDatasFromNoteHandler getAllDatasFromNoteHandler, GetAllNotesFromFolderHandler getAllNotesFromFolderHandler, GetByIdNoteHandler getByIdNoteHandler) {
        this.getAllDatasFromNoteHandler = getAllDatasFromNoteHandler;
        this.getAllNotesFromFolderHandler = getAllNotesFromFolderHandler;
        this.getByIdNoteHandler = getByIdNoteHandler;
    }
}
