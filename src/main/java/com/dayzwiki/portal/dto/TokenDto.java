package com.dayzwiki.portal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@ToString

@Getter
@Setter
public class TokenDto {
    private String token;
    private String tokenType;
    private String data;
}
