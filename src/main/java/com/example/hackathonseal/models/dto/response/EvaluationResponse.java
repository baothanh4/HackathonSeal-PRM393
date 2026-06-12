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
    private Long criterionId;
    private String criterionName;
    private Double scoreValue;
    private String feedback;
    private String internalNote;
    private Boolean isCalibration;
    private Boolean isFinalized;
    private LocalDateTime evaluatedAt;
    private LocalDateTime updatedAt;
}
