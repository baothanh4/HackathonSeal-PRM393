package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.ParticipantType;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ValidationService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final String EMAIL_PATTERN = 
            "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASSWORD_PATTERN = 
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    /**
     * Validate the registration request
     */
    public void validateRegister(RegisterRequest request) {
        // Validate full name
        validateFullName(request.getFullName());

        // Validate email
        validateEmail(request.getEmail());

        // Validate password
        validatePassword(request.getPassword());

        // Validate participant type
        validateParticipantType(request.getParticipantType());

        // If external student, validate university and student code
        if (request.getParticipantType() == ParticipantType.EXTERNAL_STUDENT) {
            validateUniversityName(request.getUniversityName());
            validateStudentCode(request.getStudentCode());
        }
    }

    /**
     * Validate full name
     */
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new AppException(ErrorCode.FULL_NAME_REQUIRED);
        }

        fullName = fullName.trim();
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new AppException(ErrorCode.FULL_NAME_INVALID);
        }
    }

    /**
     * Validate email format and uniqueness
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Email is required");
        }

        email = email.trim().toLowerCase();
        if (!emailPattern.matcher(email).matches()) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * Validate password strength
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (password.length() < 8) {
            throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (!passwordPattern.matcher(password).matches()) {
            throw new AppException(ErrorCode.PASSWORD_WEAK,
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
        }
    }

    /**
     * Validate participant type
     */
    private void validateParticipantType(ParticipantType participantType) {
        if (participantType == null) {
            throw new AppException(ErrorCode.PARTICIPANT_TYPE_REQUIRED);
        }
    }

    /**
     * Validate university name (for external students)
     */
    private void validateUniversityName(String universityName) {
        if (universityName == null || universityName.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNIVERSITY_NAME_REQUIRED);
        }

        universityName = universityName.trim();
        if (universityName.length() < 2 || universityName.length() > 100) {
            throw new AppException(ErrorCode.UNIVERSITY_NAME_INVALID);
        }
    }

    /**
     * Validate student code (for external students)
     */
    public void validateStudentCode(String studentCode) {
        if (studentCode == null || studentCode.trim().isEmpty()) {
            throw new AppException(ErrorCode.STUDENT_CODE_REQUIRED);
        }

        studentCode = studentCode.trim();
        if (studentCode.length() < 3 || studentCode.length() > 20) {
            throw new AppException(ErrorCode.STUDENT_CODE_INVALID);
        }

        if (userProfileRepository.existsByStudentCode(studentCode)) {
            throw new AppException(ErrorCode.STUDENT_CODE_ALREADY_EXISTS);
        }
    }
}

