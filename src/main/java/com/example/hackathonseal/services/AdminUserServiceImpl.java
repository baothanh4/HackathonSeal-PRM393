package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.AdminCreateUserRequest;
import com.example.hackathonseal.models.dto.response.UserAdminResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.UserProfile;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.repo.UserProfileRepository;
import com.example.hackathonseal.services.Interface.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;

    private UserAdminResponse mapToResponse(User user) {
        String studentCode = null;
        String universityName = null;

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        if (profileOpt.isPresent()) {
            studentCode = profileOpt.get().getStudentCode();
            universityName = profileOpt.get().getUniversityName();
        }

        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .studentCode(studentCode)
                .universityName(universityName)
                .build();
    }

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

        return mapToResponse(user);
    }

    @Override
    public UserAdminResponse updateStatus(Long id, com.example.hackathonseal.models.dto.request.AccountStatusRequest request) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        try {
            com.example.hackathonseal.models.Enum.AccountStatus newStatus = com.example.hackathonseal.models.Enum.AccountStatus.valueOf(request.getStatus().toUpperCase());
            u.setStatus(newStatus);
            userRepository.save(u);

            return mapToResponse(u);
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Invalid account status: " + request.getStatus());
        }
    }

    @Override
    public Page<UserAdminResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserAdminResponse getUser(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapToResponse(u);
    }
}
