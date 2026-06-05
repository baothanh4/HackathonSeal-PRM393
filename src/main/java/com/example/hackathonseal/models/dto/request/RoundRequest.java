package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoundRequest {
    @NotBlank(message = "Round name is required")
    private String name;

    private String description;

    private Integer orderIndex;

    private LocalDateTime submissionDeadline;

    private Boolean isActive;
}

