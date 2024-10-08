package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Ammunition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmmunitionRepository extends JpaRepository<Ammunition, Integer>, JpaSpecificationExecutor<Ammunition> {

    List<Ammunition> findAllByType(String type);
    Ammunition findByEnglishNameIgnoreCase(String englishName);

}
