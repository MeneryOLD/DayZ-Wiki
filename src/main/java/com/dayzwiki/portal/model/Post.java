package com.dayzwiki.portal.model;

import com.dayzwiki.portal.model.user.User;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean approved;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> postImages;


    public Post() {
        this.postImages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updateTimestamps();
    }

    public void updateTimestamps() {
        this.updatedAt = LocalDateTime.now();
    }

}
