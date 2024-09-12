package com.dayzwiki.portal.service;

import com.dayzwiki.portal.config.ValueProvider;
import com.dayzwiki.portal.exception.ApiException;
import com.dayzwiki.portal.model.user.User;
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
public class EmailService {
    private final UserService userService;
    private final ValueProvider valueProvider;
    private final JavaMailSender mailSender;

    @Async
    public void confirmRegistration(User user) {
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(user, token, "REGISTRATION");

        String recipientAddress = user.getEmail();
        String subject = "Подтверждение регистрации";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/confirm/email?token=" + token;
        String message = "Подтвердите ваш email адрес, перейдя по ссылке: ";
        sendEmail(recipientAddress, subject, message, confirmationUrl);
    }

    @Async
    public void changePassword(User user) {
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(user, token, "CHANGE");

        String recipientAddress = user.getEmail();
        String subject = "Сброс пароля на dayzwiki.net";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/reset/password?token=" + token;

        String message = "Для сброса пароля перейдите по ссылке и введите новый пароль, но не ошибись токен для изменения пароля одноразовый:";
        sendEmail(recipientAddress, subject, message, confirmationUrl);
    }

    @Async
    public void confirmEmail(User user, String newEmail) {
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(user, token, "EMAIL");

        String subject = "Confirm email for DayZ-Wiki.net";
        String confirmationUrl = valueProvider.getDayzwikiUrl() + "/api/v1/auth/confirm/email?token=" + token + "&email=" + newEmail;

        String message = "Confirm the modified email for your DayZ-Wiki account:";
        sendEmail(newEmail, subject, message, confirmationUrl);
    }

    private void sendEmail(String recipientAddress, String subject, String message, String url) {
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
