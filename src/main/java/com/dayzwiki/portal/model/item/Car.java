package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "cars")
@Getter
public class Car extends BaseItem {

    private String type;
    private String capacity;
    private Integer fuelTank;
    private Integer topSpeed;
    private String tier;
    private String seats;

    @Override
    protected String getSection() {
        return "cars";
    }
}
