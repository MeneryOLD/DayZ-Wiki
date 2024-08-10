package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.MedicationFilter;
import com.dayzwiki.portal.model.item.Medication;
import com.dayzwiki.portal.repository.item.MedicationRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public List<Medication> filterMedication(MedicationFilter medicationFilter) {
        return medicationRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), medicationFilter.getType()));

            if (medicationFilter.getDurability() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durability"), medicationFilter.getDurability()));
            }

            if (medicationFilter.getWeight() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("weight"), medicationFilter.getWeight()));
            }

            if (medicationFilter.getTier() != null && !medicationFilter.getTier().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("tier"));
                for (String level : medicationFilter.getTier()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public Medication getMedicationByEnglishName(String englishName) {
        return medicationRepository.getMedicationByEnglishName(englishName);
    }

    public List<Medication> getMedicationsByType(String type) {
        return medicationRepository.getMedicationsByType(type);
    }

}
