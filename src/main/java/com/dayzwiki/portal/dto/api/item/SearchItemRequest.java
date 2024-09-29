package com.dayzwiki.portal.dto.api.item;

import lombok.Data;
import lombok.ToString;

@ToString

@Data
public class SearchItemRequest {

    private String name;
    private String section;
    private String source;

}
