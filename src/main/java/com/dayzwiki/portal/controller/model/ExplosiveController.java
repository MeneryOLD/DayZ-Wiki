package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Explosive;
import com.dayzwiki.portal.repository.item.ExplosiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/explosives")
public class ExplosiveController {

    private final ExplosiveRepository explosiveRepository;

    @GetMapping
    public String getExplosives() {
        return "fragments/explosive";
    }

    @GetMapping("/source/{source}")
    @ResponseBody
    public List<Explosive> getExplosivesByType(@PathVariable String source) {
        return explosiveRepository.findAllBySourceIgnoreCase(source);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Explosive getExplosiveByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return explosiveRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
