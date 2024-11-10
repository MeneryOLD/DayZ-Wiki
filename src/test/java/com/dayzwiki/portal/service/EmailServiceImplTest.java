package com.dayzwiki.portal.service;

import com.dayzwiki.portal.config.ValueProvider;
import com.dayzwiki.portal.exception.ApiException;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.service.user.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EmailServiceImplTest {

    @Mock
    private VerificationService verificationService;
    @Mock
    private ValueProvider valueProvider;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("test@example.com");

        token = "sample-token";
        when(valueProvider.getDayzwikiUrl()).thenReturn("http://dayzwiki.net");

        doNothing().when(verificationService).createVerificationToken(any(User.class), anyString(), anyString());
    }

    @Test
    void testConfirmRegistration() {
        emailService.confirmRegistration(user);

        verify(verificationService, times(1)).createVerificationToken(eq(user), anyString(), eq("REGISTRATION"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testChangePassword() {
        emailService.changePassword(user);

        verify(verificationService, times(1)).createVerificationToken(eq(user), anyString(), eq("CHANGE"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testConfirmEmail() {
        String newEmail = "newEmail@example.com";

        emailService.confirmEmail(user, newEmail);

        verify(verificationService, times(1)).createVerificationToken(eq(user), anyString(), eq("EMAIL"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailSuccess() {
        // Arrange
        String recipientAddress = "recipient@example.com";
        String subject = "Test Subject";
        String message = "Test Message";
        String url = "http://testurl.com";

        emailService.sendEmail(recipientAddress, subject, message, url);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailFailure() {
        String recipientAddress = "recipient@example.com";
        String subject = "Test Subject";
        String message = "Test Message";
        String url = "http://testurl.com";

        doThrow(new MailException("Mail sending error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        ApiException exception = assertThrows(ApiException.class, () -> {
            emailService.sendEmail(recipientAddress, subject, message, url);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Error sending email", exception.getMessage());
    }
}