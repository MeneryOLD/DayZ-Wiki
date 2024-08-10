package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer>, JpaSpecificationExecutor<Food> {

    List<Food> getFoodsByType(String foodType);
    Food getFoodByEnglishName(String englishName);


}
