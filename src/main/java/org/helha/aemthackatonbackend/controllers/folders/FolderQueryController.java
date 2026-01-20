package org.helha.aemthackatonbackend.controllers.folders;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderHandler;
import org.helha.aemthackatonbackend.application.folders.query.getallfromfolder.GetAllFromFolderOutput;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderHandler;
import org.helha.aemthackatonbackend.application.notes.query.getallnotesfromfolder.GetAllNotesFromFolderOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderQueryController {
    
    private final GetAllNotesFromFolderHandler getAllNotesFromFolderHandler;
    private final GetAllFromFolderHandler getAllFromFolderHandler;
    
    @Operation(summary = "Find folders and notes from a folder")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a folder is not found",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("/folders/{folderId}/contents")
    public ResponseEntity<GetAllFromFolderOutput> getFolderContents(@PathVariable Long folderId) {
        GetAllFromFolderOutput output = getAllFromFolderHandler.handle(folderId);
        return ResponseEntity.ok(output);
    }
    
    @Operation(summary = "Find all notes from a folder")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a folder is not found",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("/{folderId}/notes")
    public ResponseEntity<GetAllNotesFromFolderOutput> getNotesByFolder(@PathVariable Long folderId) {
        GetAllNotesFromFolderOutput output = getAllNotesFromFolderHandler.handle(folderId);
        return ResponseEntity.ok(output);
    }
    
}
