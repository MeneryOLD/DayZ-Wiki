package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Explosive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExplosiveRepository extends JpaRepository<Explosive, Integer>, JpaSpecificationExecutor<Explosive> {

    List<Explosive> findAllBySourceIgnoreCase(String source);
    Explosive findByEnglishNameIgnoreCase(String englishName);

}
