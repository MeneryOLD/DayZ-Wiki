package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getNameOrEmail(), loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = tokenProvider.generateToken(authentication);

            Cookie cookie = new Cookie("dayzwiki_user_token", token);

            if (loginDto.getRemember()) {
                cookie.setMaxAge(365 * 24 * 60 * 60);
            } else {
                cookie.setMaxAge(24 * 60 * 60);
            }

            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok(new JWTAuthResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpSession session, HttpServletResponse response) {
        Cookie cookie = new Cookie("dayzwiki_user_token", "");
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}