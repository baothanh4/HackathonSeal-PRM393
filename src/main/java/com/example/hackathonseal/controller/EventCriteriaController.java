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
@RequestMapping("/api/v1/events/{eventId}/criteria")
@RequiredArgsConstructor
@Tag(name = "Event Criteria", description = "Manage customized criteria and weights for events")
public class EventCriteriaController {

    private final EventCriteriaService eventCriteriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Add customized or templated criteria to an event (ADMIN / COORDINATOR)")
    public ResponseEntity<EventCriteriaResponse> addCriterionToEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCriteriaRequest request
    ) {
        return ResponseEntity.ok(eventCriteriaService.addCriterionToEvent(eventId, request));
    }

    @GetMapping
    @Operation(summary = "Get criteria for an event")
    public ResponseEntity<List<EventCriteriaResponse>> getCriteriaForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(eventCriteriaService.getCriteriaForEvent(eventId, activeOnly));
    }

    @PutMapping("/{criterionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Update event criteria name, weight, max score or status (ADMIN / COORDINATOR)")
    public ResponseEntity<EventCriteriaResponse> updateEventCriterion(
            @PathVariable Long eventId,
            @PathVariable Long criterionId,
            @Valid @RequestBody UpdateEventCriteriaRequest request
    ) {
        return ResponseEntity.ok(eventCriteriaService.updateEventCriterion(eventId, criterionId, request));
    }

    @DeleteMapping("/{criterionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Delete event criterion (ADMIN / COORDINATOR)")
    public ResponseEntity<Void> deleteEventCriterion(
            @PathVariable Long eventId,
            @PathVariable Long criterionId
    ) {
        eventCriteriaService.deleteEventCriterion(eventId, criterionId);
        return ResponseEntity.noContent().build();
    }
}
