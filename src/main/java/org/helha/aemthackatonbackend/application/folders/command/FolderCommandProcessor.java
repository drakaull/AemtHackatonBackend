package org.helha.aemthackatonbackend.application.folders.command;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderHandler;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderInput;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderOutput;
import org.helha.aemthackatonbackend.application.folders.command.delete.DeleteFolderHandler;
import org.helha.aemthackatonbackend.application.folders.command.update.UpdateFolderHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderCommandProcessor {
    public final CreateFolderHandler createFolderHandler;
    public final DeleteFolderHandler deleteFolderHandler;
    public final UpdateFolderHandler updateFolderHandler;

    public CreateFolderOutput createFolder(CreateFolderInput input) {
        return createFolderHandler.handle(input);
    }
}
