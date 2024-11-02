package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.user.User;

public interface EmailService {
    void confirmRegistration(User user);
    void changePassword(User user);
    void confirmEmail(User user, String newEmail);
}
