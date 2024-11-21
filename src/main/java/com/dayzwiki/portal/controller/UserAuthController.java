package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.service.user.AuthorizationService;
import com.dayzwiki.portal.service.user.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        JWTAuthResponse jwtResponse = authorizationService.signIn(loginDto, response);
        if (jwtResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session, HttpServletResponse response) {
        authorizationService.logOut(session, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registration(@RequestBody SignUpDto signUpDto) {
        try {
            registrationService.registrationUser(signUpDto);
            return ResponseEntity.ok("Registration successful. Verify your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
