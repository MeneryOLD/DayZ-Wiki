package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.TokenDto;
import com.dayzwiki.portal.dto.LoginDto;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.RoleRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import com.dayzwiki.portal.service.EmailService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

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
            }
            else {
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

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {
        if (userRepository.existsByName(signUpDto.getName())) {
            return new ResponseEntity<>("Name is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setName(signUpDto.getName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setEnabled(Boolean.FALSE);

        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);
        emailService.confirmRegistration(user);
        return new ResponseEntity<>("Successful. Verify your email.", HttpStatus.OK);
    }

    @GetMapping("/change/password")
    public ResponseEntity<?> changePasswordToken(@RequestParam("email") String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            emailService.changePassword(user.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @PostMapping("/change/password")
    public ResponseEntity<?> changePassword(@RequestBody TokenDto tokenDto) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenDto.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(tokenDto.getPassword()));
        userRepository.save(user);
        verificationTokenRepository.delete(token);

        return ResponseEntity.ok().build();
    }

}
