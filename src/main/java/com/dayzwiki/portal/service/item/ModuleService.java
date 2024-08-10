package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.ModuleFilter;
import com.dayzwiki.portal.model.item.Module;
import com.dayzwiki.portal.repository.item.ModuleRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public List<Module> filterModules(ModuleFilter moduleFilter) {
        return moduleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), moduleFilter.getType()));

            if (moduleFilter.getDurability() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durability"), moduleFilter.getDurability()));
            }

            if (moduleFilter.getWeight() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("weight"), moduleFilter.getWeight()));
            }

            if (moduleFilter.getTier() != null && !moduleFilter.getTier().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("tier"));
                for (String level : moduleFilter.getTier()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public Module getModuleByEnglishName(String englishName) {
        return moduleRepository.getModuleByEnglishName(englishName);
    }

    public List<Module> getModulesByType(String type) {
        return moduleRepository.getModulesByType(type);
    }

}
