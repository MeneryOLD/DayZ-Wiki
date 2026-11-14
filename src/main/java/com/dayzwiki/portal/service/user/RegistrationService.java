package com.dayzwiki.portal.service.user;


import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.RoleRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Runs in one transaction: if the confirmation email cannot be sent, the new account is rolled back,
     * so the person can simply try again instead of finding their username already taken.
     */
    @Transactional
    public void registrationUser(SignUpDto signUpDto) {
        if (userRepository.existsByName(signUpDto.getName())) {
            throw new IllegalArgumentException("Username already taken!");
        }
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new IllegalArgumentException("Email already taken!");
        }

        User user = new User();
        user.setName(signUpDto.getName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setEnabled(Boolean.FALSE);

        Role roles = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));
        user.setRoles(List.of(roles));

        userRepository.save(user);
        emailService.confirmRegistration(user);
    }
}
