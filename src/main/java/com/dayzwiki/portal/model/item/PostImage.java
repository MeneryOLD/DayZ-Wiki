package com.dayzwiki.portal.model.item;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter

@Entity
@Table(name = "post_images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private String imageUrl;
    private LocalDateTime createdAt;

    public PostImage() {
        this.createdAt = LocalDateTime.now();
    }
}
