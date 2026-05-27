package com.example.hackthonseal.models.entity;

import com.example.hackthonseal.models.Enum.ParticipantType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ParticipantType participantType;

    @Column(unique = true, nullable = false)
    private String studentCode;

    private String universityName;


}
