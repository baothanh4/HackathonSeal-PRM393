package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.JudgeAssignmentRequest;
import com.example.hackathonseal.models.dto.response.JudgeAssignmentResponse;
import com.example.hackathonseal.services.Interface.JudgeAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/judge-assignments")
@RequiredArgsConstructor
@Tag(name = "Judge Assignments", description = "Organizers assign internal/guest judges to event rounds and categories")
public class JudgeAssignmentController {

    private final JudgeAssignmentService judgeAssignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Assign a judge to a round and category (ADMIN / COORDINATOR)")
    public ResponseEntity<JudgeAssignmentResponse> assignJudge(
            @PathVariable Long eventId,
            @Valid @RequestBody JudgeAssignmentRequest request
    ) {
        return ResponseEntity.ok(judgeAssignmentService.assignJudge(eventId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get all judge assignments for an event (ADMIN / COORDINATOR)")
    public ResponseEntity<List<JudgeAssignmentResponse>> getAssignmentsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(judgeAssignmentService.getAssignmentsForEvent(eventId));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Delete judge assignment (ADMIN / COORDINATOR)")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long eventId,
            @PathVariable Long assignmentId
    ) {
        judgeAssignmentService.deleteAssignment(eventId, assignmentId);
        return ResponseEntity.noContent().build();
    }
}
