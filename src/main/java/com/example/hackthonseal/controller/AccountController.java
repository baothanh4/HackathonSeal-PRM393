package com.example.hackthonseal.controller;

import com.example.hackthonseal.exception.AppException;
import com.example.hackthonseal.models.Enum.AccountStatus;
import com.example.hackthonseal.models.Enum.ErrorCode;
import com.example.hackthonseal.models.dto.request.AccountStatusRequest;
import com.example.hackthonseal.models.dto.response.UserAdminResponse;
import com.example.hackthonseal.models.entity.User;
import com.example.hackthonseal.repo.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Administration", description = "Admin endpoints for managing user accounts")
public class AccountController {

    private final UserRepository userRepository;

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

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (APPROVE/REJECT/SUSPEND)")
    public ResponseEntity<UserAdminResponse> updateStatus(@PathVariable Long id, @RequestBody AccountStatusRequest request) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        try {
            AccountStatus newStatus = AccountStatus.valueOf(request.getStatus().toUpperCase());
            u.setStatus(newStatus);
            userRepository.save(u);

            UserAdminResponse resp = UserAdminResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .fullName(u.getFullName())
                    .role(u.getRole() != null ? u.getRole().name() : null)
                    .status(u.getStatus() != null ? u.getStatus().name() : null)
                    .createdAt(u.getCreatedAt())
                    .build();
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Invalid account status: " + request.getStatus());
        }
    }
}

