package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.RuleRequest;
import com.example.hackathonseal.models.dto.response.RuleResponse;
import com.example.hackathonseal.services.Interface.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/rules")
@RequiredArgsConstructor
@Tag(name = "Event Rules", description = "Endpoints for managing rules and regulations associated with events")
public class RuleController {

    private final RuleService ruleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new rule to an event (ADMIN only)")
    public ResponseEntity<RuleResponse> createRule(
            @PathVariable Long eventId,
            @Valid @RequestBody RuleRequest request
    ) {
        RuleResponse response = ruleService.createRule(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get list of all rules for an event")
    public ResponseEntity<List<RuleResponse>> getRulesByEventId(
            @PathVariable Long eventId
    ) {
        List<RuleResponse> response = ruleService.getRulesByEventId(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "Get detailed rule information by ID")
    public ResponseEntity<RuleResponse> getRuleById(
            @PathVariable Long eventId,
            @PathVariable Long ruleId
    ) {
        RuleResponse response = ruleService.getRuleById(ruleId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a rule's name and description (ADMIN only)")
    public ResponseEntity<RuleResponse> updateRule(
            @PathVariable Long eventId,
            @PathVariable Long ruleId,
            @Valid @RequestBody RuleRequest request
    ) {
        RuleResponse response = ruleService.updateRule(ruleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a rule from an event (ADMIN only)")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long eventId,
            @PathVariable Long ruleId
    ) {
        ruleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
