package com.dayzwiki.portal.service;

import com.dayzwiki.portal.dto.ChangePasswordDto;
import com.dayzwiki.portal.dto.TokenDto;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePasswordServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    private User user;
    private VerificationToken token;
    private ChangePasswordDto changePasswordDto;
    private TokenDto tokenDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldPassword");

        token = new VerificationToken();
        token.setToken("validToken");
        token.setUser(user);

        changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setUsername("testUser");
        changePasswordDto.setCurrentPassword("oldPassword");
        changePasswordDto.setNewPassword("newPassword");

        tokenDto = new TokenDto();
        tokenDto.setToken("validToken");
        tokenDto.setData("newPassword");
    }

    @Test
    void testChangePasswordByEmailUserFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        changePasswordService.changePasswordByEmail("test@example.com");

        verify(emailService, times(1)).changePassword(user);
    }

    @Test
    void testChangePasswordByEmailUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            changePasswordService.changePasswordByEmail("test@example.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void testChangePasswordByTokenValid() {
        when(verificationTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        changePasswordService.changePasswordByToken(tokenDto);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).delete(token);
    }

    @Test
    void testChangePasswordByTokenNotFound() {
        when(verificationTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            changePasswordService.changePasswordByToken(new TokenDto("invalidToken", "newPassword"));
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Token not found", exception.getReason());
    }

    @Test
    void testChangePasswordByUsernameValid() {
        when(userRepository.findByName("testUser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(user)).thenReturn(user);

        changePasswordService.changePasswordByUsername(changePasswordDto);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangePasswordByUsernameUserNotFound() {
        when(userRepository.findByName("testUser")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            changePasswordService.changePasswordByUsername(changePasswordDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void testChangePasswordByUsernameCurrentPasswordIncorrect() {
        when(userRepository.findByName("testUser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            changePasswordService.changePasswordByUsername(changePasswordDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Current password is incorrect", exception.getReason());
    }
}
