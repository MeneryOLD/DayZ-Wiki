package com.dayzwiki.portal.model.item;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "explosives")
@Getter
public class Explosive extends BaseItem {

    private String type;
    private String delivery;
    private String size;
    private Float weight;
    private Integer durability;
    private String tier;

    @Override
    protected String getSection() {
        return "explosives";
    }
}
