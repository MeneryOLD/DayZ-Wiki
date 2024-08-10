package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.user.User;
import jakarta.transaction.Transactional;

public interface UserService {
    @Transactional
    void createVerificationToken(User user, String token, String type);

}
