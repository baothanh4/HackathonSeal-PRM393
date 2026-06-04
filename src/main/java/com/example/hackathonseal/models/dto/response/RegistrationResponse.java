package com.example.hackathonseal.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegistrationResponse {
    private Long registrationId;
    private Long eventId;
    private Long userId;

    // guest info if applicable
    private String guestName;
    private String guestEmail;
    private String guestStudentCode;
    private String guestUniversity;

    // tracking who registered them
    private Long registeredByUserId;
    private String registeredByUserEmail;

    // team info if applicable
    private Long teamId;
    private String teamName;

    private String message;
}

