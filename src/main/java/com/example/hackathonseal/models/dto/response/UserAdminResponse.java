package com.example.hackathonseal.models.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserAdminResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}

