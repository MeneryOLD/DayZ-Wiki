package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "explosives")
@Getter
public class Explosives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String type;
    private String size;
    private float weight;
    private int durability;
    private String tier;
    private String description;


}
