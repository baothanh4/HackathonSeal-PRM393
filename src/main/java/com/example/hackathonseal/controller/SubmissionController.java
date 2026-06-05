package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.SubmissionRequest;
import com.example.hackathonseal.models.dto.response.SubmissionResponse;
import com.example.hackathonseal.services.Interface.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/teams/{teamId}/rounds/{roundId}/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Create and manage project submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a project (attach GitHub URL). Only team leader or ADMIN can submit")
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @PathVariable Long roundId,
            @Valid @RequestBody SubmissionRequest request
    ) {
        SubmissionResponse resp = submissionService.submit(eventId, teamId, roundId, request);
        return ResponseEntity.ok(resp);
    }
}

