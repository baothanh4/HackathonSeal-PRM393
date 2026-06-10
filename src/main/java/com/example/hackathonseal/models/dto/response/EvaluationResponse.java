package com.example.hackathonseal.models.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private Long id;
    private Long submissionId;
    private Long judgeId;
    private String judgeName;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
