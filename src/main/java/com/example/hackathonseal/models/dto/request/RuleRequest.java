package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(min = 3, max = 150, message = "Rule name must be between 3 and 150 characters")
    private String name;

    @NotBlank(message = "Rule description is required")
    @Size(min = 5, max = 2000, message = "Rule description must be between 5 and 2000 characters")
    private String description;
}
