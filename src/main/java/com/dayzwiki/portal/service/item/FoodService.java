package com.dayzwiki.portal.service.item;

import com.dayzwiki.portal.dto.api.item.FoodFilter;
import com.dayzwiki.portal.model.item.Food;
import com.dayzwiki.portal.repository.item.FoodRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    public List<Food> filterFoods(FoodFilter foodFilter) {
        return foodRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("type"), foodFilter.getType()));

            if (foodFilter.getBaseCalories() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("baseCalories"), foodFilter.getBaseCalories()));
            }

            if (foodFilter.getBaseThirst() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("baseThirst"), foodFilter.getBaseThirst()));
            }

            if (foodFilter.getTier() != null && !foodFilter.getTier().isEmpty()) {
                CriteriaBuilder.In<String> insulationPredicate = criteriaBuilder.in(root.get("tier"));
                for (String level : foodFilter.getTier()) {
                    insulationPredicate.value(level);
                }
                predicates.add(insulationPredicate);
            }

            query.where(predicates.toArray(new Predicate[0]));

            return query.getRestriction();
        });
    }

    public Food getFoodByEnglishName(String englishName) {
        return foodRepository.getFoodByEnglishName(englishName);
    }

    public List<Food> getFoodsByType(String type) {
        return foodRepository.getFoodsByType(type);
    }

}
