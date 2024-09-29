package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Weapon;
import com.dayzwiki.portal.repository.item.WeaponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/weapons")
public class WeaponController {

    private final WeaponRepository weaponRepository;

    @GetMapping
    public String getWeapons() {
        return "fragments/weapon";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Weapon> getWeaponsByType(@PathVariable String type) {
        return weaponRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Weapon getWeaponByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return weaponRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
