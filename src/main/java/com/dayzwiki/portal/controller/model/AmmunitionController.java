package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Ammunition;
import com.dayzwiki.portal.repository.item.AmmunitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/ammunition")
public class AmmunitionController {

    private final AmmunitionRepository ammunitionRepository;

    @GetMapping
    public String getAmmunition() {
        return "fragments/ammunition";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Ammunition> getAmmunitionByType(@PathVariable String type) {
        return ammunitionRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Ammunition getAmmunitionByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return ammunitionRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
