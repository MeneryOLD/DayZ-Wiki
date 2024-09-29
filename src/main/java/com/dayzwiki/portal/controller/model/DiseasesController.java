package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Diseases;
import com.dayzwiki.portal.repository.item.DiseasesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/diseases")
public class DiseasesController {

    private final DiseasesRepository diseasesRepository;

    @GetMapping
    public String getDiseases() {
        return "fragments/diseases";
    }

    @GetMapping("/source/{source}")
    @ResponseBody
    public List<Diseases> getDiseasesBySource(@PathVariable String source) {
        return diseasesRepository.findAllBySourceIgnoreCase(source);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Diseases getDiseasesByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return diseasesRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
