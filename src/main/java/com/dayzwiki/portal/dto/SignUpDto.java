package com.dayzwiki.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpDto {
    private String name;
    private String email;
    private String password;

}
