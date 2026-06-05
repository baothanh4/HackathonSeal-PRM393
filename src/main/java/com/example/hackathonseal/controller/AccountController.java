package com.example.hackathonseal.controller;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.AccountStatus;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.AccountStatusRequest;
import com.example.hackathonseal.models.dto.request.AdminCreateUserRequest;
import com.example.hackathonseal.models.dto.response.UserAdminResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Administration", description = "Admin endpoints for managing user accounts")
public class AccountController {

    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINATOR')")
    @Operation(summary = "List users (paged)")
    public ResponseEntity<Page<UserAdminResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserAdminResponse> users = userRepository.findAll(pageable)
                .map(u -> UserAdminResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .status(u.getStatus() != null ? u.getStatus().name() : null)
                        .createdAt(u.getCreatedAt())
                        .build());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDINATOR')")
    @Operation(summary = "Get user details")
    public ResponseEntity<UserAdminResponse> getUser(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        UserAdminResponse resp = UserAdminResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .createdAt(u.getCreatedAt())
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an internal user account")
    public ResponseEntity<UserAdminResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserAdminResponse resp = adminUserService.createUser(request);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (APPROVE/REJECT/SUSPEND)")
    public ResponseEntity<UserAdminResponse> updateStatus(@PathVariable Long id, @RequestBody AccountStatusRequest request) {
        UserAdminResponse resp = adminUserService.updateStatus(id, request);
        return ResponseEntity.ok(resp);
    }
}

