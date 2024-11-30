package com.dayzwiki.portal.dto.api;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String value;
    private Boolean exact;
}