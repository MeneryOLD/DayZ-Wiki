package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthViewController {
    private final VerificationTokenRepository verificationTokenRepository;

    @GetMapping("/reset/password")
    public String resetPassword(@RequestParam("token") String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken.isEmpty() || !verificationToken.get().getType().equals("CHANGE")) {
            return "redirect:/error";
        }
        return "change-password";
    }

    @GetMapping("/confirm/email")
    public String confirmEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
        if (verificationTokenOptional.isEmpty() || !"REGISTRATION".equals(verificationTokenOptional.get().getType())) {
            return "redirect:error";
        }

        VerificationToken verificationToken = verificationTokenOptional.get();
        User user = verificationToken.getUser();

        user.setEnabled(Boolean.TRUE);
        verificationTokenRepository.delete(verificationToken);
        return "redirect:/";
    }


}
