package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.model.user.User;
import jakarta.transaction.Transactional;

public interface VerificationService {
    @Transactional
    void createVerificationToken(User user, String token, String type);
}
