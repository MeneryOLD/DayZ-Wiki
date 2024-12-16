package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "armors")
@Getter
public class Armor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String type;
    private String size;
    private Double weight;
    private int durability;
    private String insulation;
    private int capacity;
    private String additionalSlots;
    private String source;
    private String description;

    public String getUrlName() {
        String formattedName = englishName.replace(" ", "_").toLowerCase();
        return "https://dayzwiki.org/armors/" + formattedName;
    }

}
