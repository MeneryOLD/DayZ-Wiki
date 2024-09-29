package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Module;
import com.dayzwiki.portal.repository.item.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleRepository moduleRepository;

    @GetMapping
    public String getModules() {
        return "fragments/module";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Module> getModulesByType(@PathVariable String type) {
        return moduleRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Module getModuleByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return moduleRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}

