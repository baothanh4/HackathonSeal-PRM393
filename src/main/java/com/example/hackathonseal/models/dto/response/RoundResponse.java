package com.example.hackathonseal.models.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RoundResponse {
    private Long id;
    private Long eventId;
    private String name;
    private String description;
    private Integer orderIndex;
    private LocalDateTime submissionDeadline;
    private Boolean isActive;
}

