package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.UpdateStudentProfileRequest;
import com.example.hackathonseal.models.dto.response.StudentProfileResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.services.Interface.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student Profile Management", description = "Endpoints for students to retrieve and edit their profile details")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'STUDENT_LEADER')")
    @Operation(summary = "Get current logged-in student's profile details")
    public ResponseEntity<StudentProfileResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        StudentProfileResponse response = studentProfileService.getStudentProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'STUDENT_LEADER')")
    @Operation(summary = "Update current logged-in student's profile details")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateStudentProfileRequest request
    ) {
        StudentProfileResponse response = studentProfileService.updateStudentProfile(currentUser, request);
        return ResponseEntity.ok(response);
    }
}
