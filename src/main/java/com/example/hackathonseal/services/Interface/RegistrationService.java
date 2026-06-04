package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.response.RegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {
    RegistrationResponse register(Long eventId, Long userId, com.example.hackathonseal.models.entity.User currentUser);

    RegistrationResponse unregister(Long userId, Long eventId);

    Page<RegistrationResponse> listParticipants(Long eventId, Pageable pageable);
}

