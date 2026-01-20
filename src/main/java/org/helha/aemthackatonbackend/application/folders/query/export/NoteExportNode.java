package org.helha.aemthackatonbackend.application.folders.query.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteExportNode {
    private Long id;
    private String title;
    private String content;
}
