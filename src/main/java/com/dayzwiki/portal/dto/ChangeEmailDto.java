package com.dayzwiki.portal.dto;

import lombok.Data;

@Data
public class ChangeEmailDto {

    private String currentEmail;
    private String newEmail;

}
