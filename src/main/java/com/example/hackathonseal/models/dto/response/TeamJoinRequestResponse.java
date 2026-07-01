package com.example.hackathonseal.models.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamJoinRequestResponse {
    private Long requestId;
    private Long teamId;
    private String teamName;
    private Long registrationId;
    private Long userId;
    private String fullName;
    private String email;
    private String studentCode;
    private String university;
    private String status;
    private LocalDateTime createdAt;
}
