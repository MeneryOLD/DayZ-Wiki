package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import com.dayzwiki.portal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/auth")
public class EmailChangeController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @GetMapping("/change/email")
    @ResponseBody
    public ResponseEntity<?> changeEmailToken(Principal principal, @RequestParam("email") String email, @RequestParam("newEmail") String newEmail) {
        if (!principal.getName().equals(email) || principal.getName().equals(newEmail)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Uncorrect email address");
        }
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && !userRepository.existsByEmail(newEmail)) {
            emailService.confirmEmail(user.get(), newEmail);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or email already used");
    }

    @GetMapping("/confirm/email")
    public String confirmEmail(@RequestParam("token") String token, @RequestParam("email") String email) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        User user = verificationToken.getUser();
        user.setEmail(email);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return "home";
    }

}