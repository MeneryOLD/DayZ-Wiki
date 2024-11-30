package com.dayzwiki.portal.repository;

import com.dayzwiki.portal.model.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, String>, JpaSpecificationExecutor<YoutubeVideo> {
    YoutubeVideo findTopByActiveTrueOrderByOrderAsc();
}
