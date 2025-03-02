package com.dayzwiki.portal.dto.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Order {
    private String field;
    private String direction;

    public Order(String pair) {
        List<String> strings = Arrays.stream(pair.split(" ")).toList();
        this.field = strings.get(0);
        this.direction = strings.get(1);
    }
}