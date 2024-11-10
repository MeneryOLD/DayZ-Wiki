package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.SignUpDto;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.RoleRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.service.user.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
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

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationService(userRepository, roleRepository, passwordEncoder, emailService);
    }

    @Test
    void testRegisterUserSuccess() {
        SignUpDto signUpDto = new SignUpDto("testUser", "test@example.com", "password");
        Role userRole = new Role("USER");

        when(userRepository.existsByNameOrEmail(signUpDto.getName(), signUpDto.getEmail())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(signUpDto.getPassword())).thenReturn("encodedPassword");

        ResponseEntity<?> response = registrationService.registrationUser(signUpDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Successful. Verify your email.", response.getBody());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).confirmRegistration(any(User.class));
    }

    @Test
    void testRegisterUserUsernameAlreadyTaken() {
        SignUpDto signUpDto = new SignUpDto("existingUser", "new@example.com", "password");

        when(userRepository.existsByName(signUpDto.getName())).thenReturn(true);

        ResponseEntity<?> response = registrationService.registrationUser(signUpDto);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already taken!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).confirmRegistration(any(User.class));
    }

    @Test
    void testRegisterUserEmailAlreadyTaken() {
        SignUpDto signUpDto = new SignUpDto("newUser", "existing@example.com", "password");

        when(userRepository.existsByEmail(signUpDto.getEmail())).thenReturn(true);

        ResponseEntity<?> response = registrationService.registrationUser(signUpDto);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email already taken!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).confirmRegistration(any(User.class));
    }

}
