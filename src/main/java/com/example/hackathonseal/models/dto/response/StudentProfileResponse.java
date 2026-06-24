package com.example.hackathonseal.models.dto.response;

import com.example.hackathonseal.models.Enum.ParticipantType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private ParticipantType participantType;
    private String studentCode;
    private String universityName;
    private LocalDateTime createdAt;
}
