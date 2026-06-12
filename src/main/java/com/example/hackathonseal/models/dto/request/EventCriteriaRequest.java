package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventCriteriaRequest {
    private Long templateId;
    private String customName;

    @Min(value = 0, message = "Weight must be positive")
    private Double customWeight;

    @Min(value = 0, message = "Max score must be positive")
    private Double maxScore;
}
