package org.helha.aemthackatonbackend.application.folders.command.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateFolderOutput {
    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CreateFolderOutput(long id,
                              @NotBlank String name,
                              long parentId,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt
    ) {}
}
