package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.ChangePasswordDto;
import com.dayzwiki.portal.dto.TokenDto;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;


    public void changePasswordByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        emailService.changePassword(user.get());
    }

    public void changePasswordByToken(TokenDto tokenDto) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenDto.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(tokenDto.getData()));
        userRepository.save(user);
        verificationTokenRepository.delete(token);
    }

    public void changePasswordByUsername(ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByName(changePasswordDto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

}
