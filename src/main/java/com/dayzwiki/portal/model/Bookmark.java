package com.dayzwiki.portal.model;

import com.dayzwiki.portal.model.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "english_name", nullable = false)
    private String englishName;

    @Column(name = "description")
    private String description;

    @Column(name = "section", nullable = false)
    private String section;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "url", nullable = false)
    private String url;

}
