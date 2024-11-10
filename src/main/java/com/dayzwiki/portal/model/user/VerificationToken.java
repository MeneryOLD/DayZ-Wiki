package com.dayzwiki.portal.model.user;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class VerificationToken {
    public static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date expiryDate;

    @Column(nullable = false)
    private String type;

    public VerificationToken(User user, String token, String type) {
        this.user = user;
        this.token = token;
        this.type = type;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public static Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}