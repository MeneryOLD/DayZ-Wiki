package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.WeaponFilter;
import com.dayzwiki.portal.model.item.Weapon;
import com.dayzwiki.portal.repository.item.WeaponRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class WeaponService {
    private final WeaponRepository weaponRepository;

    public List<Weapon> filterWeapons(WeaponFilter weaponFilter) {
        return weaponRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), weaponFilter.getType()));

            if (weaponFilter.getDamage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("damage"), weaponFilter.getDamage()));
            }

            if (weaponFilter.getDurability() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durability"), weaponFilter.getDurability()));
            }

            if (weaponFilter.getTier() != null && !weaponFilter.getTier().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("tier"));
                for (String level : weaponFilter.getTier()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public Weapon getWeaponByEnglishName(String englishName) {
        return weaponRepository.getWeaponByEnglishName(englishName);
    }

    public List<Weapon> getWeaponsByType(String type) {
        return weaponRepository.getWeaponsByType(type);
    }

}
