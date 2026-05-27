package com.example.hackthonseal.services;

import com.example.hackthonseal.exception.AppException;
import com.example.hackthonseal.models.Enum.AccountStatus;
import com.example.hackthonseal.models.Enum.ParticipantType;
import com.example.hackthonseal.models.Enum.UserRole;
import com.example.hackthonseal.models.dto.request.LoginRequest;
import com.example.hackthonseal.models.dto.request.RegisterRequest;
import com.example.hackthonseal.models.dto.response.AuthResponse;
import com.example.hackthonseal.models.dto.response.RegisterResponse;
import com.example.hackthonseal.models.entity.User;
import com.example.hackthonseal.models.entity.UserProfile;
import com.example.hackthonseal.repo.UserRepository;
import com.example.hackthonseal.repo.UserProfileRepository;
import com.example.hackthonseal.services.Interface.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ValidationService validationService;

    @Override
    public AuthResponse login(LoginRequest request){
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        ));

        boolean matches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!matches) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (user.getStatus()
                != AccountStatus.APPROVED) {

            throw new RuntimeException(
                    "Account not approved"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .id(user.getId())
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate input
        validationService.validateRegister(request);

        // Create user
        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(UserRole.STUDENT)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // If external student, create user profile
        if (request.getParticipantType() == ParticipantType.EXTERNAL_STUDENT) {
            UserProfile userProfile = UserProfile.builder()
                    .user(user)
                    .participantType(ParticipantType.EXTERNAL_STUDENT)
                    .studentCode(request.getStudentCode().trim())
                    .universityName(request.getUniversityName().trim())
                    .build();
            userProfileRepository.save(userProfile);
        } else {
            // Create user profile for FPT students with null values for optional fields
            UserProfile userProfile = UserProfile.builder()
                    .user(user)
                    .participantType(ParticipantType.FPT_STUDENT)
                    .universityName("FPT University")
                    .studentCode(request.getStudentCode().trim())
                    .build();
            userProfileRepository.save(userProfile);
        }

        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Registration successful")
                .status("APPROVED")
                .build();
    }
}



