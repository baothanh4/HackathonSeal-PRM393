package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriterionScoreRequest {
    @NotNull(message = "Criterion ID is required")
    private Long criterionId;

    @NotNull(message = "Score value is required")
    @Min(value = 0, message = "Score must be at least 0")
    private Double scoreValue;

    private String feedback;

    private String internalNote;

    private Boolean isCalibration = false;

    private Boolean isFinalized = false;
}
