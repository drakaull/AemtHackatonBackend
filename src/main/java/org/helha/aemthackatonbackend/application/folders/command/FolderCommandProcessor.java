package org.helha.aemthackatonbackend.application.folders.command;

import lombok.RequiredArgsConstructor;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderHandler;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderInput;
import org.helha.aemthackatonbackend.application.folders.command.create.CreateFolderOutput;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderCommandProcessor {
    public final CreateFolderHandler createFolderHandler;

    public CreateFolderOutput createFolder(CreateFolderInput input){
        return createFolderHandler.handle(input);
    }
}
