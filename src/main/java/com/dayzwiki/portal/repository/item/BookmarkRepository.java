package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.Bookmark;
import com.dayzwiki.portal.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, JpaSpecificationExecutor<Bookmark> {

    List<Bookmark> findAllByUserId(Long userId);
    Integer countAllByUserId(Long userId);
    boolean existsByUserAndUrl(User user, String url);
}
