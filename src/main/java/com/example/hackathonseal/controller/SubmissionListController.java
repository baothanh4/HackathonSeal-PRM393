package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.response.SubmissionResponse;
import com.example.hackathonseal.services.Interface.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions Management", description = "Endpoints for admins to list submissions")
public class SubmissionListController {

    private final SubmissionService submissionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINATOR') or hasRole('JUDGE')")
    @Operation(summary = "List all project submissions (ADMIN/COORDINATOR/JUDGE)")
    public ResponseEntity<List<SubmissionResponse>> getAllSubmissions() {
        List<SubmissionResponse> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }
}
