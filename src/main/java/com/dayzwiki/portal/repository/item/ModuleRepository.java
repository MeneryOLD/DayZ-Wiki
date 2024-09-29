package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer>, JpaSpecificationExecutor<Module> {

    List<Module> findAllByType(String type);
    Module findByEnglishNameIgnoreCase(String englishName);

}
