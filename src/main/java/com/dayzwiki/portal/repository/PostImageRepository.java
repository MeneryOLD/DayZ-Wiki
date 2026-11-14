package com.dayzwiki.portal.repository;

import com.dayzwiki.portal.model.PostImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Integer>, JpaSpecificationExecutor<PostImage> {

    @Transactional
    void deleteAllByPostId(Integer postId);
    List<PostImage> findAllByPostId(Integer postId);

    /** Image ids of a post, oldest first (does not load the image bytes). */
    @Query("select i.id from PostImage i where i.post.id = :postId order by i.id")
    List<Integer> findIdsByPostId(@Param("postId") Integer postId);

    /** Rows of [postId, first image id, image count] for the given posts. */
    @Query("select i.post.id, min(i.id), count(i.id) from PostImage i where i.post.id in :postIds group by i.post.id")
    List<Object[]> findCovers(@Param("postIds") Collection<Integer> postIds);

}
