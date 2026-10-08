package com.internship.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    public enum Role { STUDENT, COMPANY, ADMIN }
    public enum Status { ACTIVE, PENDING, LOCKED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;
    @Column(length = 500) private String skills;
    private Instant createdAt = Instant.now();
}
