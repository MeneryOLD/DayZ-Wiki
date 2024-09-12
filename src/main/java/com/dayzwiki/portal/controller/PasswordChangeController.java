package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.ChangePasswordDto;
import com.dayzwiki.portal.dto.TokenDto;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import com.dayzwiki.portal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class PasswordChangeController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;

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
    public ResponseEntity<?> changePasswordByToken(@RequestBody TokenDto tokenDto) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenDto.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(tokenDto.getData()));
        userRepository.save(user);
        verificationTokenRepository.delete(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/change/password/by-username")
    public ResponseEntity<?> changePasswordByUsername(@RequestBody ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByName(changePasswordDto.getUsername()).get();

        if (passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User current password is incorrect");
    }

}
