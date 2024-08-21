package com.dayzwiki.portal.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bans")
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ban_reason", nullable = false)
    private String banReason;

    @Column(name = "ban_date", nullable = false)
    private LocalDateTime banDate = LocalDateTime.now();

    @Column(name = "unban_date")
    private LocalDateTime unbanDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "admin_name", nullable = false)
    private String adminName;
}

