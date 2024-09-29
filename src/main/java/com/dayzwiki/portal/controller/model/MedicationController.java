package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Medication;
import com.dayzwiki.portal.repository.item.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/medication")
public class MedicationController {

    private final MedicationRepository medicationRepository;

    @GetMapping
    public String getMedication() {
        return "fragments/medication";
    }

    @GetMapping("/type/{type}")
    @ResponseBody
    public List<Medication> getMedicationByType(@PathVariable String type) {
        return medicationRepository.findAllByType(type);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Medication getMedicationByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return medicationRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
