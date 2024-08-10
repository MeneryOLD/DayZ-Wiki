package com.dayzwiki.portal.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dayzwiki.portal.model.user.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailOrName(String email, String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);

    boolean existsByName(String username);
    boolean existsByEmail(String email);
}
