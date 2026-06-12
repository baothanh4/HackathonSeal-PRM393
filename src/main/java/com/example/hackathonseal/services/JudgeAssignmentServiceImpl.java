package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.JudgeType;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.JudgeAssignmentRequest;
import com.example.hackathonseal.models.dto.response.JudgeAssignmentResponse;
import com.example.hackathonseal.models.entity.*;
import com.example.hackathonseal.repo.*;
import com.example.hackathonseal.services.Interface.JudgeAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeAssignmentServiceImpl implements JudgeAssignmentService {

    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RoundRepository roundRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public JudgeAssignmentResponse assignJudge(Long eventId, JudgeAssignmentRequest request) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        User judge = userRepository.findById(request.getJudgeId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Judge not found"));

        if (judge.getRole() != UserRole.JUDGE) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User is not a judge");
        }

        Round round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round not found"));

        if (!round.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round does not belong to this event");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));
            if (!category.getEvent().getId().equals(eventId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category does not belong to this event");
            }
        }

        JudgeType type;
        try {
            type = JudgeType.valueOf(request.getJudgeType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Invalid judge type (must be INTERNAL or GUEST)");
        }

        // Check for existing assignment
        List<JudgeAssignment> existing = judgeAssignmentRepository.findByRoundIdAndJudgeId(round.getId(), judge.getId());
        final Category finalCategory = category;
        boolean alreadyAssigned = existing.stream().anyMatch(a -> {
            if (finalCategory == null) {
                return a.getCategory() == null;
            } else {
                return a.getCategory() != null && a.getCategory().getId().equals(finalCategory.getId());
            }
        });

        if (alreadyAssigned) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Judge is already assigned to this round/category");
        }

        JudgeAssignment assignment = JudgeAssignment.builder()
                .judge(judge)
                .round(round)
                .category(category)
                .judgeType(type)
                .build();

        assignment = judgeAssignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeAssignmentResponse> getAssignmentsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        return judgeAssignmentRepository.findByRoundEventId(eventId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAssignment(Long eventId, Long assignmentId) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        JudgeAssignment assignment = judgeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment not found"));

        if (!assignment.getRound().getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Assignment does not belong to this event");
        }

        judgeAssignmentRepository.delete(assignment);
    }

    private JudgeAssignmentResponse mapToResponse(JudgeAssignment assignment) {
        return JudgeAssignmentResponse.builder()
                .id(assignment.getId())
                .judgeId(assignment.getJudge().getId())
                .judgeName(assignment.getJudge().getFullName())
                .judgeEmail(assignment.getJudge().getEmail())
                .roundId(assignment.getRound().getId())
                .roundName(assignment.getRound().getName())
                .categoryId(assignment.getCategory() != null ? assignment.getCategory().getId() : null)
                .categoryName(assignment.getCategory() != null ? assignment.getCategory().getName() : null)
                .judgeType(assignment.getJudgeType().name())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}
