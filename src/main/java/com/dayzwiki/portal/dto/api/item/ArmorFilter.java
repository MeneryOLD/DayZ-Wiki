package com.dayzwiki.portal.dto.api.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ArmorFilter {
    private String type;
    private Integer minDurability;
    private Integer minCapacity;
    private Double minWeight;
    private List<String> insulation;
}