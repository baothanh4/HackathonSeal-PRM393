package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgeAssignmentRequest {
    @NotNull(message = "Judge ID is required")
    private Long judgeId;

    @NotNull(message = "Round ID is required")
    private Long roundId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Judge type is required")
    private String judgeType;
}
