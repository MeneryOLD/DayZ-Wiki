package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "weapons")
@Getter
public class Weapon extends BaseItem {

    private String type;
    private String size;
    private Float weight;
    private String tier;
    private Integer damage;
    private Integer heavyDamage;
    private String damageType;
    private Integer durability;
    @Column(columnDefinition = "TEXT")
    private String ammunition;
    private String fireModes;
    @Column(columnDefinition = "TEXT")
    private String magazines;

    @ManyToOne
    @JoinColumn(name = "caliber", referencedColumnName = "id")
    private Ammunition caliber;

    @Override
    protected String getSection() {
        return "weapons";
    }
}
