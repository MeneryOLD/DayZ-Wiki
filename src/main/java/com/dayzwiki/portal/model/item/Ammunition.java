package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "ammunition")
@Getter
public class Ammunition extends BaseItem {

    private String type;
    private String size;
    private Float weight;
    private String tier;
    private Float healthDamage;
    private Float bloodDamage;
    private Float shockDamage;
    @Column(columnDefinition = "TEXT")
    private String usedBy;

    @Override
    protected String getSection() {
        return "ammunition";
    }
}
