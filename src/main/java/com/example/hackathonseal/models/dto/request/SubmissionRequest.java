package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionRequest {
    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "GitHub URL is required")
    private String githubUrl;
}

