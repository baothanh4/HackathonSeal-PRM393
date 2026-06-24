package com.example.hackathonseal.models.dto.request;

import com.example.hackathonseal.models.Enum.ParticipantType;
import com.example.hackathonseal.validation.annotation.ValidFullName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentProfileRequest {

    @NotBlank(message = "Full name is required")
    @ValidFullName
    private String fullName;

    @NotNull(message = "Participant type is required")
    private ParticipantType participantType;

    private String universityName;

    @NotBlank(message = "Student code is required")
    private String studentCode;
}
