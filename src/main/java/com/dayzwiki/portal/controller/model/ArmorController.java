package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Armor;
import com.dayzwiki.portal.repository.item.ArmorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/armors")
public class ArmorController {

    private final ArmorRepository armorRepository;

    @GetMapping
    public String getArmors() {
        return "fragments/armor";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Armor> getArmorsByType(@PathVariable String type) {
        return armorRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Armor getArmorByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return armorRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
