package com.example.hackathonseal.models.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCriteriaResponse {
    private Long id;
    private Long eventId;
    private Long templateId;
    private String customName;
    private Double customWeight;
    private Double maxScore;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
