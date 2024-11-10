package com.dayzwiki.portal.service;

import com.dayzwiki.portal.config.ValueProvider;
import com.dayzwiki.portal.exception.ApiException;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import com.dayzwiki.portal.service.user.VerificationService;
import com.dayzwiki.portal.service.user.VerificationServiceImpl;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EmailServiceImpl implements EmailService {
    private final VerificationService verificationService;
    private final ValueProvider valueProvider;
    private final JavaMailSender mailSender;

    @Async
    @Override
    public void confirmRegistration(User user) {
        String token = UUID.randomUUID().toString();
        verificationService.createVerificationToken(user, token, "REGISTRATION");

        String recipientAddress = user.getEmail();
        String subject = "Confirmation of registration";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/confirm/email?token=" + token;
        String message = "Confirm your email address by clicking the link: ";
        sendEmail(recipientAddress, subject, message, confirmationUrl);
    }

    @Async
    @Override
    public void changePassword(User user) {
        String token = UUID.randomUUID().toString();
        verificationService.createVerificationToken(user, token, "CHANGE");

        String recipientAddress = user.getEmail();
        String subject = "Resetting the password to dayzwiki.net";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/reset/password?token=" + token;

        String message = "To reset your password, follow the link and enter a new password: ";
        sendEmail(recipientAddress, subject, message, confirmationUrl);
    }

    @Async
    @Override
    public void confirmEmail(User user, String newEmail) {
        String token = UUID.randomUUID().toString();
        verificationService.createVerificationToken(user, token, "EMAIL");

        String subject = "Confirm email for DayZ-Wiki.net";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/api/v1/auth/confirm/email?token=" + token + "&email=" + newEmail;

        String message = "Confirm the modified email for your DayZ-Wiki account:";
        sendEmail(newEmail, subject, message, confirmationUrl);
    }

    @Async
    protected void sendEmail(String recipientAddress, String subject, String message, String url) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setFrom("support@dayzwiki.net");
        email.setSubject(subject);
        email.setText(String.format("%s %s", message, url));
        try {
            mailSender.send(email);
        } catch (MailException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error sending email");
        }
    }

}
