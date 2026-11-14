package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "armors")
@Getter
public class Armor extends BaseItem {

    private String type;
    private String size;
    private Double weight;
    private Integer durability;
    private String insulation;
    private Integer capacity;
    @Column(columnDefinition = "TEXT")
    private String additionalSlots;
    private String protection;
    private String tier;

    @Override
    protected String getSection() {
        return "armors";
    }
}
