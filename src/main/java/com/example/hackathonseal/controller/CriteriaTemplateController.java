package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.CriteriaTemplateRequest;
import com.example.hackathonseal.models.dto.response.CriteriaTemplateResponse;
import com.example.hackathonseal.services.Interface.CriteriaTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criteria-templates")
@RequiredArgsConstructor
@Tag(name = "Criteria Templates", description = "Manage default criteria templates across events")
public class CriteriaTemplateController {

    private final CriteriaTemplateService criteriaTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Create a default criteria template (ADMIN / COORDINATOR)")
    public ResponseEntity<CriteriaTemplateResponse> createTemplate(@Valid @RequestBody CriteriaTemplateRequest request) {
        return ResponseEntity.ok(criteriaTemplateService.createTemplate(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get all criteria templates (ADMIN / COORDINATOR)")
    public ResponseEntity<List<CriteriaTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(criteriaTemplateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get a criteria template by ID (ADMIN / COORDINATOR)")
    public ResponseEntity<CriteriaTemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(criteriaTemplateService.getTemplateById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Update a criteria template by ID (ADMIN / COORDINATOR)")
    public ResponseEntity<CriteriaTemplateResponse> updateTemplate(@PathVariable Long id, @Valid @RequestBody CriteriaTemplateRequest request) {
        return ResponseEntity.ok(criteriaTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Delete a criteria template by ID (ADMIN / COORDINATOR)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        criteriaTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
