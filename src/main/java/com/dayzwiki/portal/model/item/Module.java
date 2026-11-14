package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "modules")
@Getter
public class Module extends BaseItem {

    private String type;
    private Float weight;
    private String size;
    private String tier;
    private Integer durability;
    private Float magnification;
    private Integer capacity;
    @Column(columnDefinition = "TEXT")
    private String usedBy;

    @Override
    protected String getSection() {
        return "modules";
    }
}
