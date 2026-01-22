package org.helha.aemthackatonbackend.infrastructure.folder;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IFolderRepository extends CrudRepository<DbFolder, Long> {
    
    //  List<DbFolder> findByName(String name);
    List<DbFolder> findByParentId(Long parentId);
    
    boolean existsByParentIdAndNameIgnoreCase(Long parentId, String name);
    
    boolean existsByParentIdAndNameIgnoreCaseAndIdNot(Long parentId, String name, Long id);
}
