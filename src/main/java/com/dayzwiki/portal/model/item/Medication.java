package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "medicaments")
@Getter
public class Medication extends BaseItem {

    private String type;
    private String tier;
    private String size;
    private Integer durability;
    private Float weight;
    @Column(columnDefinition = "TEXT")
    private String medicalUse;
    private String numberOfUses;

    @Override
    protected String getSection() {
        return "medication";
    }
}
