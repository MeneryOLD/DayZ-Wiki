package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "cars")
@Getter
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String englishName;
    private String altName;

    private String capacity;
    private int fuelTank;
    private int maxSpeed;
    private String tier;
    private String seats;
    private String description;

}


