package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Integer>, JpaSpecificationExecutor<Medication> {
    List<Medication> findAllByType(String type);
    Medication findByEnglishNameIgnoreCase(String englishName);

}
