package com.dayzwiki.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@Data
public class ValueProvider {
    @Value("${dayzwiki.url}")
    private String dayzwikiUrl;
}