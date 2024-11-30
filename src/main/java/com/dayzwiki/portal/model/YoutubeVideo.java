package com.dayzwiki.portal.model;

import com.dayzwiki.portal.model.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter

@ToString
@AllArgsConstructor
@Entity
@Table(name = "youtube_videos")
public class YoutubeVideo {
    @Id
    @Column(columnDefinition = "varchar(12)")
    private String id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private boolean active;

    @Column(name = "`order`")
    private int order;

    @JoinColumn(updatable = false)
    private LocalDateTime created;

    @JoinColumn(updatable = false)
    private LocalDateTime expiryDate;

    public YoutubeVideo() {
        this.created = LocalDateTime.now();
    }

}
