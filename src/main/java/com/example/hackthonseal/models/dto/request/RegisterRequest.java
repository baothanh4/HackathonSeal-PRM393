package com.example.hackthonseal.models.dto.request;

import com.example.hackthonseal.models.Enum.ParticipantType;
import com.example.hackthonseal.validation.annotation.ValidFullName;
import com.example.hackthonseal.validation.annotation.ValidPassword;
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


