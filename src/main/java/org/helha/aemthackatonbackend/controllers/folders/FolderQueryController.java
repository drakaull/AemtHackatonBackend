package org.helha.aemthackatonbackend.controllers.folders;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderHandler;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderQueryController {
    
    private final GetAllFromFolderHandler getAllFromFolderHandler;
    
    @Operation(summary = "Find folders and notes from a folder")
    @GetMapping("/folders/{folderId}/contents")
    public ResponseEntity<GetAllFromFolderOutput> getFolderContents(@PathVariable Long folderId) {
        GetAllFromFolderOutput output = getAllFromFolderHandler.handle(folderId);
        return ResponseEntity.ok(output);
    }
    
}
