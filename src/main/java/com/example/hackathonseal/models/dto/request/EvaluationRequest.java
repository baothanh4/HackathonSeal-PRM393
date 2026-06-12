package com.example.hackathonseal.models.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EvaluationRequest {
    @NotNull(message = "Scores list is required")
    @Valid
    private List<CriterionScoreRequest> scores;
}
