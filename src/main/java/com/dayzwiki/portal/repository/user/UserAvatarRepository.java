package com.dayzwiki.portal.repository.user;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
    UserAvatar findByUserId(long userId);
}