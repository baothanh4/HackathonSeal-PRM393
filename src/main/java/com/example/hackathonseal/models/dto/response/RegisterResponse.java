package com.example.hackathonseal.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private String fullName;
    private String message;
    private String status;
}

