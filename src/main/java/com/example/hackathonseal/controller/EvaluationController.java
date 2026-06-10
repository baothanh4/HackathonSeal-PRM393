package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.EvaluationRequest;
import com.example.hackathonseal.models.dto.response.EvaluationResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.services.Interface.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/submissions/{submissionId}/evaluations")
@RequiredArgsConstructor
@Tag(name = "Evaluations", description = "Evaluate project submissions")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit score and feedback for a submission. Only assigned Judges or ADMIN/COORDINATOR")
    public ResponseEntity<EvaluationResponse> evaluate(
            @PathVariable Long eventId,
            @PathVariable Long submissionId,
            @Valid @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(evaluationService.evaluateSubmission(eventId, submissionId, request, currentUser));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all evaluations for a submission. Only accessible by Judges assigned to Category, or ADMIN/COORDINATOR")
    public ResponseEntity<List<EvaluationResponse>> getEvaluations(
            @PathVariable Long eventId,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(evaluationService.getEvaluationsBySubmission(eventId, submissionId, currentUser));
    }
}
