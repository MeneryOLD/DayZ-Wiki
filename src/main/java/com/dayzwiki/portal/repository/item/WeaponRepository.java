package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, Integer>, JpaSpecificationExecutor<Weapon> {

    List<Weapon> findAllByType(String type);
    Weapon findByEnglishNameIgnoreCase(String englishName);

}
