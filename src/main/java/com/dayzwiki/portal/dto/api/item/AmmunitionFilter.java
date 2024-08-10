package com.dayzwiki.portal.dto.api.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AmmunitionFilter {
    private String type;
    private Double healthDamage;
    private Double shockDamage;
    private List<String> tier;
}
