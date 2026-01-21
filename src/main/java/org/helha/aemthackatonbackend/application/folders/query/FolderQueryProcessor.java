package org.helha.aemthackatonbackend.application.folders.query;

import org.helha.aemthackatonbackend.application.folders.query.export.ExportFolderTreeHandler;
import org.helha.aemthackatonbackend.application.folders.query.export.FolderExportNode;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderHandler;
import org.helha.aemthackatonbackend.application.folders.query.getfolderbyid.GetFolderByIdHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    public final GetAllFromFolderHandler getAllFromFolderHandler;
    public final ExportFolderTreeHandler exportFolderTreeHandler;
    public final GetFolderByIdHandler getFolderByIdHandler;
    
    public FolderQueryProcessor(GetAllFromFolderHandler getAllFromFolderHandler, ExportFolderTreeHandler exportFolderTreeHandler, GetFolderByIdHandler getFolderByIdHandler) {
        this.getAllFromFolderHandler = getAllFromFolderHandler;
        this.exportFolderTreeHandler = exportFolderTreeHandler;
        this.getFolderByIdHandler = getFolderByIdHandler;
    }

    public FolderExportNode exportNode(Long folderId){
        return exportFolderTreeHandler.handle(folderId);
    }
}
