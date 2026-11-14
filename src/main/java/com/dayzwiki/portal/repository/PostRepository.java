package com.dayzwiki.portal.repository;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, JpaSpecificationExecutor<Post> {
    List<Post> findAllByApproved(Boolean approved);

    List<Post> findAllByApprovedOrderByCreatedAtDesc(boolean approved);

    List<Post> findAllByAuthorOrderByCreatedAtDesc(User author);

    long countByApproved(boolean approved);

    long countByAuthorAndApproved(User author, boolean approved);
}
