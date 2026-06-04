package com.example.hackathonseal.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStatusRequest {
    @NotBlank
    private String status;
}

