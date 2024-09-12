package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.PostImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Integer>, JpaSpecificationExecutor<PostImage> {

    @Transactional
    void deleteAllByPostId(Integer postId);

    List<PostImage> findAllByPostId(Integer postId);

}
