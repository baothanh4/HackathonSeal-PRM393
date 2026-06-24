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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private StudentProfileServiceImpl studentProfileService;

    private User studentUser;
    private UserProfile studentProfile;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id(1L)
                .email("student@gmail.com")
                .fullName("John Doe")
                .role(UserRole.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();

        studentProfile = UserProfile.builder()
                .id(1L)
                .user(studentUser)
                .participantType(ParticipantType.FPT_STUDENT)
                .studentCode("SE160001")
                .universityName("FPT University")
                .build();
    }

    @Test
    void getStudentProfile_Success() {
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));

        StudentProfileResponse response = studentProfileService.getStudentProfile(studentUser);

        assertNotNull(response);
        assertEquals("student@gmail.com", response.getEmail());
        assertEquals("John Doe", response.getFullName());
        assertEquals("STUDENT", response.getRole());
        assertEquals(ParticipantType.FPT_STUDENT, response.getParticipantType());
        assertEquals("SE160001", response.getStudentCode());
        assertEquals("FPT University", response.getUniversityName());
    }

    @Test
    void getStudentProfile_NotStudentRole_ThrowsException() {
        User adminUser = User.builder()
                .id(2L)
                .email("admin@gmail.com")
                .role(UserRole.ADMIN)
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                studentProfileService.getStudentProfile(adminUser)
        );

        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), exception.getErrorCode());
    }

    @Test
    void updateStudentProfile_Success() {
        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .fullName("John Updated")
                .participantType(ParticipantType.EXTERNAL_STUDENT)
                .universityName("VNU University")
                .studentCode("ET160002")
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(userProfileRepository.findByStudentCode("ET160002")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));
        when(userRepository.save(any(User.class))).thenReturn(studentUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(studentProfile);

        StudentProfileResponse response = studentProfileService.updateStudentProfile(studentUser, request);

        assertNotNull(response);
        assertEquals("John Updated", response.getFullName());
        assertEquals(ParticipantType.EXTERNAL_STUDENT, response.getParticipantType());
        assertEquals("ET160002", response.getStudentCode());
        assertEquals("VNU University", response.getUniversityName());
    }

    @Test
    void updateStudentProfile_StudentCodeTakenByOther_ThrowsException() {
        UpdateStudentProfileRequest request = UpdateStudentProfileRequest.builder()
                .fullName("John Updated")
                .participantType(ParticipantType.EXTERNAL_STUDENT)
                .universityName("VNU University")
                .studentCode("ET160002")
                .build();

        User otherUser = User.builder().id(2L).email("other@gmail.com").build();
        UserProfile otherProfile = UserProfile.builder()
                .id(2L)
                .user(otherUser)
                .studentCode("ET160002")
                .build();

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(userProfileRepository.findByStudentCode("ET160002")).thenReturn(Optional.of(otherProfile));

        AppException exception = assertThrows(AppException.class, () ->
                studentProfileService.updateStudentProfile(studentUser, request)
        );

        assertEquals(ErrorCode.STUDENT_CODE_ALREADY_EXISTS.getCode(), exception.getErrorCode());
    }
}
