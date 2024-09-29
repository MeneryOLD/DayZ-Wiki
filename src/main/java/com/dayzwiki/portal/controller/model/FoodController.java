package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Food;
import com.dayzwiki.portal.repository.item.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/foods")
public class FoodController {

    private final FoodRepository foodRepository;

    @GetMapping
    public String getFoods() {
        return "fragments/food";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Food> getFoodsByType(@PathVariable String type) {
        return foodRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Food getFoodByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return foodRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
