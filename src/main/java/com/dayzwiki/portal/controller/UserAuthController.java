package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.service.user.AuthorizationService;
import com.dayzwiki.portal.service.user.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final AuthorizationService authorizationService;
    private final RegistrationService registrationService;

    @PostMapping("/signin")
    public ResponseEntity<JWTAuthResponse> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        return authorizationService.signIn(loginDto, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        return authorizationService.logOut(session, response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registration(@RequestBody SignUpDto signUpDto) {
        return registrationService.registrationUser(signUpDto);
    }
}