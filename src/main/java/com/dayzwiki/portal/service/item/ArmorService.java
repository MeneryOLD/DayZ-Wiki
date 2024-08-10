package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.ArmorFilter;
import com.dayzwiki.portal.model.item.Armor;
import com.dayzwiki.portal.repository.item.ArmorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArmorService {

    private final ArmorRepository armorRepository;

    public List<Armor> filterArmors(ArmorFilter armorFilter) {
        return armorRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), armorFilter.getType()));

            if (armorFilter.getMinDurability() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durability"), armorFilter.getMinDurability()));
            }

            if (armorFilter.getMinCapacity() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), armorFilter.getMinCapacity()));
            }

            if (armorFilter.getMinWeight() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("weight"), armorFilter.getMinWeight()));
            }

            if (armorFilter.getInsulation() != null && !armorFilter.getInsulation().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("insulation"));
                for (String level : armorFilter.getInsulation()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public Armor getArmorByEnglishName(String englishName) {
        return armorRepository.getArmorByEnglishName(englishName);
    }

    public List<Armor> getArmorsByType(String type) {
        return armorRepository.getArmorsByType(type);
    }

}