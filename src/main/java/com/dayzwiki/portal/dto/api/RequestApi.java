package com.dayzwiki.portal.dto.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString

@Getter
@Setter
@NoArgsConstructor
public class RequestApi {
    public Integer page;
    public Integer size = -1;
    public SearchRequest search;
    public List<Order> orders;
    public String type;
}
