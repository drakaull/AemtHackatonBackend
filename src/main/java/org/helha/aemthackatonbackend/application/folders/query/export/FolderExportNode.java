package org.helha.aemthackatonbackend.application.folders.query.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderExportNode {
    private Long id;
    private String name;
    private List<FolderExportNode> children;
    private List<NoteExportNode> notes;
}
