package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.exception.ApiException;
import com.dayzwiki.portal.security.JWTAuthResponse;
import com.dayzwiki.portal.service.user.AuthorizationService;
import com.dayzwiki.portal.service.user.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final AuthorizationService authorizationService;
    private final RegistrationService registrationService;

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        JWTAuthResponse jwtResponse;
        try {
            jwtResponse = authorizationService.signIn(loginDto, response);
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Confirm your email first: open the link we sent you.");
        }
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
        String name = signUpDto.getName() == null ? "" : signUpDto.getName().trim();
        String email = signUpDto.getEmail() == null ? "" : signUpDto.getEmail().trim();
        String password = signUpDto.getPassword() == null ? "" : signUpDto.getPassword();

        if (!NAME.matcher(name).matches()) {
            return ResponseEntity.badRequest().body("Username must be 3-32 characters: letters, numbers, _ . -");
        }
        if (!EMAIL.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Incorrect email format");
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }
        try {
            registrationService.registrationUser(new SignUpDto(name, email, password));
            return ResponseEntity.ok("Registration successful. Verify your email.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Could not send the confirmation email. Try again later.");
        }
    }

    private static final Pattern NAME = Pattern.compile("[\\p{L}\\p{N}_.-]{3,32}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

}
