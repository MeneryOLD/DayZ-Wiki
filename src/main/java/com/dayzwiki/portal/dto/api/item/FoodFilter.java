package com.dayzwiki.portal.dto.api.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class FoodFilter {
    private String type;
    private Integer baseCalories;
    private Integer baseThirst;
    private List<String> tier;
}
