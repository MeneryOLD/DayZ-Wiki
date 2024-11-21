package com.dayzwiki.portal.dto.api.item;

import lombok.Data;

@Data
public class SearchItemRequest {
    private String name;
    private String section;
    private String source;
}
