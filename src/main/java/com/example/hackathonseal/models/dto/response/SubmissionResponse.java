package com.example.hackathonseal.models.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SubmissionResponse {
    private Long id;
    private Long teamId;
    private Long roundId;
    private String projectName;
    private String githubUrl;
    private String status;
    private LocalDateTime submittedAt;

    private String teamName;
    private String trackName;
    private String roundName;
    private String eventName;
    private Integer teamSize;
}

