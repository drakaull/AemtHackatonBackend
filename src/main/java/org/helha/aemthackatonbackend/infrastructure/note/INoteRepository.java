package org.helha.aemthackatonbackend.infrastructure.note;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface INoteRepository extends CrudRepository<DbNote, Long> {
    
    //    List<DbNote> findByName(String name);
    List<DbNote> findByFolderId(Long folderId);
}
