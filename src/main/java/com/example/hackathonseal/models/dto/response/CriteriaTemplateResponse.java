package com.example.hackathonseal.models.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private Double defaultMaxScore;
    private Double defaultWeight;
    private LocalDateTime createdAt;
}
