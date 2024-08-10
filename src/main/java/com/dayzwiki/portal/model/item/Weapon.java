package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "weapons")
@Getter
public class Weapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String type;
    private String size;
    private float weight;

    @ManyToOne
    @JoinColumn(name = "caliber", referencedColumnName = "id")
    private Ammunition caliber;
    private String tier;
    private int damage;
    private String description;
    private int durability;

}
