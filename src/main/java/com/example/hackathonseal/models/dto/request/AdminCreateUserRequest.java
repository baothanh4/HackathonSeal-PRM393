package com.example.hackathonseal.models.dto.request;

import com.example.hackathonseal.models.Enum.AccountStatus;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    /**
     * Optional. If not provided, defaults to APPROVED for internal accounts.
     */
    private AccountStatus status;
}

