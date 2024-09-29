package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Armor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmorRepository extends JpaRepository<Armor, Integer>, JpaSpecificationExecutor<Armor> {

    List<Armor> findAllByType(String type);
    Armor findByEnglishNameIgnoreCase(String englishName);

}
