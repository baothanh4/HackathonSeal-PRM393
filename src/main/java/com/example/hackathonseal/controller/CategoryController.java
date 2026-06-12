package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.CategoryRequest;
import com.example.hackathonseal.models.dto.response.CategoryResponse;
import com.example.hackathonseal.services.Interface.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Manage Event Categories, Mentors, and Judges")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Create a new category in an event. Only ADMIN or COORDINATOR")
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable Long eventId,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.createCategory(eventId, request));
    }

    @GetMapping
    @Operation(summary = "Get all categories of an event")
    public ResponseEntity<List<CategoryResponse>> getCategories(@PathVariable Long eventId) {
        return ResponseEntity.ok(categoryService.getCategoriesByEvent(eventId));
    }

    @PostMapping("/{categoryId}/mentors/{mentorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Assign a mentor to a category. Only ADMIN or COORDINATOR")
    public ResponseEntity<CategoryResponse> assignMentor(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @PathVariable Long mentorId
    ) {
        return ResponseEntity.ok(categoryService.assignMentorToCategory(eventId, categoryId, mentorId));
    }

    @PostMapping("/{categoryId}/judges/{judgeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Assign a judge to a category. Only ADMIN or COORDINATOR")
    public ResponseEntity<CategoryResponse> assignJudge(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @PathVariable Long judgeId
    ) {
        return ResponseEntity.ok(categoryService.assignJudgeToCategory(eventId, categoryId, judgeId));
    }
}
