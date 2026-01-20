package org.helha.aemthackatonbackend.application.folders.query;

import org.helha.aemthackatonbackend.application.folders.query.export.ExportFolderTreeHandler;
import org.helha.aemthackatonbackend.application.folders.query.export.FolderExportNode;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    public final GetAllFromFolderHandler getAllFromFolderHandler;
    private final ExportFolderTreeHandler exportFolderTreeHandler;
    
    public FolderQueryProcessor(GetAllFromFolderHandler getAllFromFolderHandler, ExportFolderTreeHandler exportFolderTreeHandler) {
        this.getAllFromFolderHandler = getAllFromFolderHandler;
        this.exportFolderTreeHandler = exportFolderTreeHandler;
    }

    public FolderExportNode exportNode(Long folderId){
        return exportFolderTreeHandler.handle(folderId);
    }
}
