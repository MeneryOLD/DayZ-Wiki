package com.dayzwiki.portal.security;

import org.springframework.security.core.Authentication;

public interface JwtTokenProvider {
    String generateToken(Authentication authentication);
    String getUsernameFromJWT(String token);
    boolean validateToken(String token);
}