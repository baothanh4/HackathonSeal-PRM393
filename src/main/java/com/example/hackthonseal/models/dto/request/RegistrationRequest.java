package com.example.hackthonseal.models.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    // Either provide an existing userId OR provide guest details (guestEmail required for guest)
    private Long userId;

    // Guest fields (used when registering a person without an account)
    @Email(message = "Invalid email format for guest")
    private String guestEmail;

    private String guestName;

    private String guestStudentCode;
    private String guestUniversity;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValidRequest() {
        // Either userId or guestEmail must be provided
        boolean hasUserId = userId != null;
        boolean hasGuestEmail = guestEmail != null && !guestEmail.isBlank();
        return hasUserId || hasGuestEmail;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getValidationError() {
        if (!isValidRequest()) {
            return "Either userId or guestEmail must be provided";
        }
        return null;
    }
}

