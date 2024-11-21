package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.RoleRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.service.user.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        SignUpDto signUpDto = new SignUpDto("testUser", "test@example.com", "password");

        when(userRepository.existsByName(signUpDto.getName())).thenReturn(false);
        when(userRepository.existsByEmail(signUpDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signUpDto.getPassword())).thenReturn("encodedPassword");

        Role userRole = new Role("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        registrationService.registrationUser(signUpDto);

        verify(userRepository).save(any(User.class));
        verify(emailService).confirmRegistration(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        SignUpDto signUpDto = new SignUpDto("existingUser", "new@example.com", "password");

        when(userRepository.existsByName(signUpDto.getName())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registrationUser(signUpDto);
        });

        assertEquals("Username already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        SignUpDto signUpDto = new SignUpDto("newUser", "existing@example.com", "password");

        when(userRepository.existsByName(signUpDto.getName())).thenReturn(false);
        when(userRepository.existsByEmail(signUpDto.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registrationUser(signUpDto);
        });

        assertEquals("Email already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        SignUpDto signUpDto = new SignUpDto("newUser", "new@example.com", "password");

        when(userRepository.existsByName(signUpDto.getName())).thenReturn(false);
        when(userRepository.existsByEmail(signUpDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            registrationService.registrationUser(signUpDto);
        });

        assertEquals("Role 'USER' not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
