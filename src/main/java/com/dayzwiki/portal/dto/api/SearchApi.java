package com.dayzwiki.portal.dto.api;

import lombok.*;


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
