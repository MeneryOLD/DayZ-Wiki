package com.dayzwiki.portal.config;

import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the account roles on startup if they are missing.
 * Registration gives every new account the USER role, so it must exist even on a fresh database.
 */
@Component
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

    static final List<String> ROLES = List.of("USER", "MODER", "ADMIN");

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        ROLES.stream()
                .filter(name -> roleRepository.findByName(name).isEmpty())
                .forEach(name -> roleRepository.save(new Role(name)));
    }
}
