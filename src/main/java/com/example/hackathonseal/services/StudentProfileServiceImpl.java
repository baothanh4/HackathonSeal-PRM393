package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.ParticipantType;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.UpdateStudentProfileRequest;
import com.example.hackathonseal.models.dto.response.StudentProfileResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.UserProfile;
import com.example.hackathonseal.repo.UserProfileRepository;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public StudentProfileResponse getStudentProfile(User currentUser) {
        if (currentUser.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only students can view/edit student profiles");
        }

        UserProfile userProfile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Student profile not found"));

        return convertToResponse(currentUser, userProfile);
    }

    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(User currentUser, UpdateStudentProfileRequest request) {
        if (currentUser.getRole() != UserRole.STUDENT) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only students can view/edit student profiles");
        }

        UserProfile userProfile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Student profile not found"));

        // Validate fullName
        String fullName = request.getFullName().trim();
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new AppException(ErrorCode.FULL_NAME_INVALID);
        }

        // Validate participant type & university
        ParticipantType participantType = request.getParticipantType();
        String universityName;
        if (participantType == ParticipantType.EXTERNAL_STUDENT) {
            if (request.getUniversityName() == null || request.getUniversityName().trim().isEmpty()) {
                throw new AppException(ErrorCode.UNIVERSITY_NAME_REQUIRED);
            }
            universityName = request.getUniversityName().trim();
            if (universityName.length() < 2 || universityName.length() > 100) {
                throw new AppException(ErrorCode.UNIVERSITY_NAME_INVALID);
            }
        } else {
            universityName = "FPT University";
        }

        // Validate student code
        String studentCode = request.getStudentCode().trim();
        if (studentCode.length() < 3 || studentCode.length() > 20) {
            throw new AppException(ErrorCode.STUDENT_CODE_INVALID);
        }

        // Check if studentCode is taken by another profile
        userProfileRepository.findByStudentCode(studentCode)
                .filter(profile -> !profile.getUser().getId().equals(currentUser.getId()))
                .ifPresent(profile -> {
                    throw new AppException(ErrorCode.STUDENT_CODE_ALREADY_EXISTS);
                });

        // Update user entity
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        user.setFullName(fullName);
        userRepository.save(user);

        // Update profile entity
        userProfile.setParticipantType(participantType);
        userProfile.setStudentCode(studentCode);
        userProfile.setUniversityName(universityName);
        userProfileRepository.save(userProfile);

        return convertToResponse(user, userProfile);
    }

    private StudentProfileResponse convertToResponse(User user, UserProfile userProfile) {
        return StudentProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .participantType(userProfile.getParticipantType())
                .studentCode(userProfile.getStudentCode())
                .universityName(userProfile.getUniversityName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
