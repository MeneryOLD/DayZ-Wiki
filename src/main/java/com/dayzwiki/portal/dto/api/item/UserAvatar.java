package com.dayzwiki.portal.dto.api.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "user_avatars")
public class UserAvatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "avatar", nullable = false)
    private byte[] avatar;

}
