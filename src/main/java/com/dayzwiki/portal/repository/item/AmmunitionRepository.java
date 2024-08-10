package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Ammunition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmmunitionRepository extends JpaRepository<Ammunition, Integer>, JpaSpecificationExecutor<Ammunition> {

    Ammunition getAmmunitionByEnglishName(String englishName);
    List<Ammunition> getAmmunitionsByType(String type);

}
