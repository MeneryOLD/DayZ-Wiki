package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "ammunition")
@Getter
public class Ammunition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private float healthDamage;
    private float bloodDamage;
    private float shockDamage;

    private String type;
    private String size;
    private String tier;
    private String source;
    private String description;

    public String getUrlName() {
        String formattedName = englishName.replace(" ", "_").toLowerCase();
        return "https://dayzwiki.org/ammunition/" + formattedName;
    }

}
