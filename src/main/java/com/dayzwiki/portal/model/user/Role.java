package com.dayzwiki.portal.model.user;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}