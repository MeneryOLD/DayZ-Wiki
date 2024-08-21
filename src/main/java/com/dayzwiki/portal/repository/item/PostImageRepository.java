package com.dayzwiki.portal.repository.item;

import com.dayzwiki.portal.model.item.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Integer>, JpaSpecificationExecutor<PostImage> {
}
