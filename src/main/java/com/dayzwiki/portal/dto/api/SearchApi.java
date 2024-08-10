package com.dayzwiki.portal.dto.api;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchApi {
    private Object name;
    private Object englishName;
    private Object section;
    private Object url;
    private Object description;
    private Object source;
}
