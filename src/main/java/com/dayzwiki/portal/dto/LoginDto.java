package com.dayzwiki.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginDto {
    private String nameOrEmail;
    private String password;
    private Boolean remember;
}
