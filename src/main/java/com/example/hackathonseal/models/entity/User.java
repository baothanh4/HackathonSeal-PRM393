package com.example.hackathonseal.models.entity;


import com.example.hackathonseal.models.Enum.AccountStatus;
import com.example.hackathonseal.models.Enum.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserRole  role;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private Boolean isEmailVerified;

    private LocalDateTime createdAt;

}
