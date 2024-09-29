package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Diseases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseasesRepository extends JpaRepository<Diseases, Integer>, JpaSpecificationExecutor<Diseases> {

    List<Diseases> findAllBySourceIgnoreCase(String source);
    Diseases findByEnglishNameIgnoreCase(String englishName);

}
