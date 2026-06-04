package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.RegistrationRequest;
import com.example.hackathonseal.models.dto.response.RegistrationResponse;
import com.example.hackathonseal.services.Interface.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.hackathonseal.models.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Registration", description = "Endpoints to register/unregister users for events")
@lombok.extern.slf4j.Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register a user for an event")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @Valid @RequestBody RegistrationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("Received event registration request for eventId={}, userId={}, requesterEmail={}",
                eventId, request.getUserId(), currentUser != null ? currentUser.getEmail() : "anonymous");

        RegistrationResponse response = registrationService.register(
                        eventId,
                        request.getUserId(),
                        currentUser);
        log.info("Event registration successful: registrationId={}, eventId={}", response.getRegistrationId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{eventId}/registrations/{userId}")
    @Operation(summary = "Unregister a user from an event")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<RegistrationResponse> unregister(
            @PathVariable Long eventId,
            @PathVariable Long userId
    ) {
        RegistrationResponse response = registrationService.unregister(userId, eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/registrations")
    @Operation(summary = "List event participants")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINATOR')")
    public ResponseEntity<Page<RegistrationResponse>> listParticipants(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RegistrationResponse> participants = registrationService.listParticipants(eventId, pageable);
        return ResponseEntity.ok(participants);
    }
}

