package com.example.hackathonseal.models.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    // Optional. If not provided, registers the currently logged-in user.
    private Long userId;
}
