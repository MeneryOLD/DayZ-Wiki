package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "foods")
@Getter
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private int baseCalories;
    private int baseThirst;

    private String type;
    private String tier;
    private int durability;
    private String size;
    private float weight;
    private String description;


}
