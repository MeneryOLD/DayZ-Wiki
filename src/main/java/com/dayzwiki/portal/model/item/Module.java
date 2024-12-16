package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Table(name = "modules")
@Entity
@Getter
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String altName;
    private String englishName;

    private float weight;
    private String size;
    private String tier;
    private int durability;
    private String type;
    private float magnification;
    private int capacity;
    private String source;
    private String description;

    public String getUrlName() {
        String formattedName = englishName.replace(" ", "_").toLowerCase();
        return "https://dayzwiki.org/modules/" + formattedName;
    }

}
