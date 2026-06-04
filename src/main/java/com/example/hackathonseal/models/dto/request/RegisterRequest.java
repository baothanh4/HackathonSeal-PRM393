package com.example.hackathonseal.models.dto.request;

import com.example.hackathonseal.models.Enum.ParticipantType;
import com.example.hackathonseal.validation.annotation.ValidFullName;
import com.example.hackathonseal.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @ValidFullName
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Participant type is required")
    private ParticipantType participantType;

    private String universityName;

    private String studentCode;
}


