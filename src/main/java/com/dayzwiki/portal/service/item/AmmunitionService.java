package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.AmmunitionFilter;
import com.dayzwiki.portal.model.item.Ammunition;
import com.dayzwiki.portal.repository.item.AmmunitionRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmmunitionService {

    private final AmmunitionRepository ammunitionRepository;

    public List<Ammunition> filterAmmunition(AmmunitionFilter ammunitionFilter) {
        return ammunitionRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), ammunitionFilter.getType()));

            if (ammunitionFilter.getHealthDamage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("healthDamage"), ammunitionFilter.getHealthDamage()));
            }

            if (ammunitionFilter.getShockDamage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("shockDamage"), ammunitionFilter.getShockDamage()));
            }

            if (ammunitionFilter.getTier() != null && !ammunitionFilter.getTier().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("tier"));
                for (String level : ammunitionFilter.getTier()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public List<Ammunition> getAmmunitionsByType(String type) {
        return ammunitionRepository.getAmmunitionsByType(type);
    }

    public Ammunition getAmmunitionByEnglishName(String englishName) {
        return ammunitionRepository.getAmmunitionByEnglishName(englishName);
    }

}
