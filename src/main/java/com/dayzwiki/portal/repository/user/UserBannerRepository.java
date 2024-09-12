package com.dayzwiki.portal.repository.user;

import com.dayzwiki.portal.dto.api.item.UserBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBannerRepository extends JpaRepository<UserBanner, Long> {
    UserBanner findByUserId(long userId);
}
