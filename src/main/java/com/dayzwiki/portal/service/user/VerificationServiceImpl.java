package com.dayzwiki.portal.service.user;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.model.user.VerificationToken;
import com.dayzwiki.portal.repository.user.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final VerificationTokenRepository tokenRepository;

    @Override
    public void createVerificationToken(User user, String token, String type) {
        VerificationToken myToken = new VerificationToken(user, token, type);
        tokenRepository.save(myToken);
    }

}
