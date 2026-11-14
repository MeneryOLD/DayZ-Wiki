package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

/** Resolves the signed-in account (if any) and its permissions. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Optional<User> find(Principal principal) {
        if (principal == null || principal instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        String name = principal.getName();
        return userRepository.findByEmailOrName(name, name);
    }

    public static boolean isModerator(Principal principal) {
        if (!(principal instanceof Authentication auth)) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_MODER") || a.equals("ROLE_ADMIN"));
    }

    public static boolean sameUser(User a, User b) {
        return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
    }
}
