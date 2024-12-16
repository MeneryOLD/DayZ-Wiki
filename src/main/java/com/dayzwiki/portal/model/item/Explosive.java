package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "explosives")
@Getter
public class Explosive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String size;
    private float weight;
    private int durability;
    private String tier;
    private String source;
    private String description;

    public String getUrlName() {
        String formattedName = englishName.replace(" ", "_").toLowerCase();
        return "https://dayzwiki.org/explosives/" + formattedName;
    }

}
