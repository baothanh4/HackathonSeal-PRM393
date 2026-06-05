package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.RoundRequest;
import com.example.hackathonseal.models.dto.response.RoundResponse;
import com.example.hackathonseal.services.Interface.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/rounds")
@RequiredArgsConstructor
@Tag(name = "Rounds", description = "Manage rounds and submission windows")
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a round for an event (ADMIN)")
    public ResponseEntity<RoundResponse> createRound(@PathVariable Long eventId, @Valid @RequestBody RoundRequest request) {
        RoundResponse resp = roundService.createRound(eventId, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    @Operation(summary = "List rounds for an event")
    public ResponseEntity<List<RoundResponse>> getRounds(@PathVariable Long eventId) {
        List<RoundResponse> resp = roundService.getRoundsForEvent(eventId);
        return ResponseEntity.ok(resp);
    }
}

