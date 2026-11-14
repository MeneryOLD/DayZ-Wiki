package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "foods")
@Getter
public class Food extends BaseItem {

    private String type;
    private Integer baseCalories;
    private Integer baseThirst;
    private Integer stomach;
    private String states;
    private String tier;
    private Integer durability;
    private String size;
    private Float weight;

    @Override
    protected String getSection() {
        return "foods";
    }
}
