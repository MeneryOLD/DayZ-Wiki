package com.dayzwiki.portal.dto.api.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class WeaponFilter {
    private String type;
    private Integer damage;
    private Integer durability;
    private List<String> tier;
}
