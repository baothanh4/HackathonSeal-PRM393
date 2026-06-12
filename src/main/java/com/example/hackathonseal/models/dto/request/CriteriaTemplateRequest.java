package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriteriaTemplateRequest {
    @NotBlank(message = "Criteria name is required")
    private String name;

    private String description;

    @NotNull(message = "Default max score is required")
    @Positive(message = "Default max score must be positive")
    private Double defaultMaxScore;

    @NotNull(message = "Default weight is required")
    @Positive(message = "Default weight must be positive")
    private Double defaultWeight;
}
