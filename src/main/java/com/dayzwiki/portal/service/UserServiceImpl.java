package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final VerificationTokenRepository tokenRepository;

    @Override
    public void createVerificationToken(User user, String token, String type) {
        VerificationToken myToken = new VerificationToken(user, token, type);
        tokenRepository.save(myToken);
    }

}
