package com.hannofleet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", schema = "hannofleet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String token;
    private Long tokenExpires;

    public enum Role {
        ADMIN,
        USER
    }
}
