package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.EventCriteriaRequest;
import com.example.hackathonseal.models.dto.request.UpdateEventCriteriaRequest;
import com.example.hackathonseal.models.dto.response.EventCriteriaResponse;
import com.example.hackathonseal.services.Interface.EventCriteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds/{roundId}/criteria")
@RequiredArgsConstructor
@Tag(name = "Event Criteria", description = "Manage customized criteria and weights for event rounds")
public class EventCriteriaController {

    private final EventCriteriaService eventCriteriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Add customized or templated criteria to a round (ADMIN / COORDINATOR)")
    public ResponseEntity<EventCriteriaResponse> addCriterionToRound(
            @PathVariable Long roundId,
            @Valid @RequestBody EventCriteriaRequest request
    ) {
        return ResponseEntity.ok(eventCriteriaService.addCriterionToRound(roundId, request));
    }

    @GetMapping
    @Operation(summary = "Get criteria for a round")
    public ResponseEntity<List<EventCriteriaResponse>> getCriteriaForRound(
            @PathVariable Long roundId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(eventCriteriaService.getCriteriaForRound(roundId, activeOnly));
    }

    @PutMapping("/{criterionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Update round criteria name, weight, max score or status (ADMIN / COORDINATOR)")
    public ResponseEntity<EventCriteriaResponse> updateEventCriterion(
            @PathVariable Long roundId,
            @PathVariable Long criterionId,
            @Valid @RequestBody UpdateEventCriteriaRequest request
    ) {
        return ResponseEntity.ok(eventCriteriaService.updateEventCriterion(roundId, criterionId, request));
    }

    @DeleteMapping("/{criterionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Delete round criterion (ADMIN / COORDINATOR)")
    public ResponseEntity<Void> deleteEventCriterion(
            @PathVariable Long roundId,
            @PathVariable Long criterionId
    ) {
        eventCriteriaService.deleteEventCriterion(roundId, criterionId);
        return ResponseEntity.noContent().build();
    }
}
