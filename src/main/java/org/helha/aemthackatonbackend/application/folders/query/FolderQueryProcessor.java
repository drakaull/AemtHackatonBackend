package org.helha.aemthackatonbackend.application.folders.query;

import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    public final GetAllFromFolderHandler getAllFromFolderHandler;
    
    public FolderQueryProcessor(GetAllFromFolderHandler getAllFromFolderHandler) {
        this.getAllFromFolderHandler = getAllFromFolderHandler;
    }
}
