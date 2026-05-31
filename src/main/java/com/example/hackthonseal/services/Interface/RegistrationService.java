package com.example.hackthonseal.services.Interface;

import com.example.hackthonseal.models.dto.response.RegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationService {
    RegistrationResponse register(Long eventId, Long userId, String guestName, String guestEmail, String guestStudentCode, String guestUniversity, com.example.hackthonseal.models.entity.User currentUser);

    RegistrationResponse unregister(Long userId, Long eventId);

    Page<RegistrationResponse> listParticipants(Long eventId, Pageable pageable);
}

