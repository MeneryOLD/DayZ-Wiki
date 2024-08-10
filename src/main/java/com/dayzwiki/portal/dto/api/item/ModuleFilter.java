package com.dayzwiki.portal.dto.api.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ModuleFilter {
    private String type;
    private Double weight;
    private Integer durability;
    private List<String> tier;

}
