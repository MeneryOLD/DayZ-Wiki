package com.dayzwiki.portal.controller.model;

import com.dayzwiki.portal.model.item.Car;
import com.dayzwiki.portal.repository.item.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarRepository carRepository;

    @GetMapping
    public String getCars() {
        return "fragments/car";
    }

    @GetMapping("/source/{source}")
    @ResponseBody
    public List<Car> getCarsBySource(@PathVariable String source) {
        return carRepository.findAllBySourceIgnoreCase(source);
    }

    @GetMapping("/{englishName}")
    @ResponseBody
    public Car getCarByEnglishName(@PathVariable String englishName) {
        String formattedName = englishName.replace("_", " ");
        return carRepository.findByEnglishNameIgnoreCase(formattedName);
    }

}
