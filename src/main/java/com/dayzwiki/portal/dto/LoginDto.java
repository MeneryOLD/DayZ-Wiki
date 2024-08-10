package com.dayzwiki.portal.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String nameOrEmail;
    private String password;
    private Boolean remember;
}
