package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.AdminCreateUserRequest;
import com.example.hackathonseal.models.dto.response.UserAdminResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserAdminResponse createUser(AdminCreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        com.example.hackathonseal.models.Enum.AccountStatus status =
                request.getStatus() != null ? request.getStatus() : com.example.hackathonseal.models.Enum.AccountStatus.APPROVED;

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(request.getRole())
                .status(status)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserAdminResponse updateStatus(Long id, com.example.hackathonseal.models.dto.request.AccountStatusRequest request) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        try {
            com.example.hackathonseal.models.Enum.AccountStatus newStatus = com.example.hackathonseal.models.Enum.AccountStatus.valueOf(request.getStatus().toUpperCase());
            u.setStatus(newStatus);
            userRepository.save(u);

            return UserAdminResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .fullName(u.getFullName())
                    .role(u.getRole() != null ? u.getRole().name() : null)
                    .status(u.getStatus() != null ? u.getStatus().name() : null)
                    .createdAt(u.getCreatedAt())
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Invalid account status: " + request.getStatus());
        }
    }

    @Override
    public Page<UserAdminResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(u -> UserAdminResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .role(u.getRole() != null ? u.getRole().name() : null)
                        .status(u.getStatus() != null ? u.getStatus().name() : null)
                        .createdAt(u.getCreatedAt())
                        .build());
    }

    @Override
    public UserAdminResponse getUser(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return UserAdminResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .createdAt(u.getCreatedAt())
                .build();
    }
}

