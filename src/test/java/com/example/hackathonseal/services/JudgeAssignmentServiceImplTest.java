package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.JudgeType;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.JudgeAssignmentRequest;
import com.example.hackathonseal.models.dto.response.JudgeAssignmentResponse;
import com.example.hackathonseal.models.entity.*;
import com.example.hackathonseal.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudgeAssignmentServiceImplTest {

    @Mock
    private JudgeAssignmentRepository judgeAssignmentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private JudgeAssignmentServiceImpl judgeAssignmentService;

    private Event event;
    private User judge;
    private Round round;
    private Category category;
    private JudgeAssignmentRequest request;
    private JudgeAssignment assignment;

    @BeforeEach
    void setUp() {
        event = Event.builder().id(1L).build();

        judge = User.builder()
                .id(10L)
                .email("judge@gmail.com")
                .fullName("Judge One")
                .role(UserRole.JUDGE)
                .build();

        round = Round.builder()
                .id(2L)
                .event(event)
                .name("Round 1")
                .build();

        category = Category.builder()
                .id(3L)
                .event(event)
                .name("Web Dev")
                .build();

        request = new JudgeAssignmentRequest();
        request.setJudgeId(10L);
        request.setRoundId(2L);
        request.setCategoryId(3L);
        request.setJudgeType("GUEST");

        assignment = JudgeAssignment.builder()
                .id(100L)
                .judge(judge)
                .round(round)
                .category(category)
                .judgeType(JudgeType.GUEST)
                .build();
    }

    @Test
    void assignJudge_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(judge));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(judgeAssignmentRepository.findByRoundIdAndJudgeId(2L, 10L)).thenReturn(Collections.emptyList());
        when(judgeAssignmentRepository.save(any(JudgeAssignment.class))).thenAnswer(invocation -> {
            JudgeAssignment saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        JudgeAssignmentResponse response = judgeAssignmentService.assignJudge(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("GUEST", response.getJudgeType());
    }

    @Test
    void updateAssignment_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(judgeAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(judge));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(judgeAssignmentRepository.findByRoundIdAndJudgeId(2L, 10L)).thenReturn(new ArrayList<>(List.of(assignment)));
        when(judgeAssignmentRepository.save(any(JudgeAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        request.setJudgeType("INTERNAL");

        JudgeAssignmentResponse response = judgeAssignmentService.updateAssignment(1L, 100L, request);

        assertNotNull(response);
        assertEquals("INTERNAL", response.getJudgeType());
    }

    @Test
    void updateAssignment_AlreadyAssigned_ThrowsException() {
        JudgeAssignment otherAssignment = JudgeAssignment.builder()
                .id(200L)
                .judge(judge)
                .round(round)
                .category(category)
                .build();

        when(eventRepository.existsById(1L)).thenReturn(true);
        when(judgeAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(userRepository.findById(10L)).thenReturn(Optional.of(judge));
        when(roundRepository.findById(2L)).thenReturn(Optional.of(round));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(judgeAssignmentRepository.findByRoundIdAndJudgeId(2L, 10L)).thenReturn(List.of(assignment, otherAssignment));

        AppException exception = assertThrows(AppException.class, () ->
                judgeAssignmentService.updateAssignment(1L, 100L, request)
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), exception.getErrorCode());
        assertEquals("Judge is already assigned to this round/category", exception.getMessage());
    }

    @Test
    void deleteAssignment_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(judgeAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        doNothing().when(judgeAssignmentRepository).delete(assignment);

        assertDoesNotThrow(() -> judgeAssignmentService.deleteAssignment(1L, 100L));
        verify(judgeAssignmentRepository, times(1)).delete(assignment);
    }
}
