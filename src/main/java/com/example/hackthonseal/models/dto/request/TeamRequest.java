package com.example.hackthonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRequest {
    @NotBlank(message = "Team name is required")
    private String name;
}
