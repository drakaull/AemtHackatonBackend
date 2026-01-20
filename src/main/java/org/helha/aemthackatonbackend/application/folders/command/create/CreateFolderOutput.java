package org.helha.aemthackatonbackend.application.folders.command.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderOutput {
    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
