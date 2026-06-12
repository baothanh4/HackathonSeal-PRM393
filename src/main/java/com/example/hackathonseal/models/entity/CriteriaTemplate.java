package com.example.hackathonseal.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "criteria_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriteriaTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criteria_name", nullable = false)
    private String name;

    @Column(name = "criteria_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_max_score")
    private Double defaultMaxScore;

    @Column(name = "default_weight")
    private Double defaultWeight;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
