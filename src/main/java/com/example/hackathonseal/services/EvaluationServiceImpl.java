package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.EvaluationRequest;
import com.example.hackathonseal.models.dto.response.EvaluationResponse;
import com.example.hackathonseal.models.entity.Category;
import com.example.hackathonseal.models.entity.Evaluation;
import com.example.hackathonseal.models.entity.Submission;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.EvaluationRepository;
import com.example.hackathonseal.repo.SubmissionRepository;
import com.example.hackathonseal.services.Interface.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public EvaluationResponse evaluateSubmission(Long eventId, Long submissionId, EvaluationRequest request, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        if (submission.getRound() == null || submission.getRound().getEvent() == null ||
                !submission.getRound().getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission does not belong to this event");
        }

        // Validate Judge permissions
        validateJudgeAssignment(submission, currentUser);

        // Check if already evaluated by this judge, if so update it
        Optional<Evaluation> existingOpt = evaluationRepository.findBySubmissionAndJudge(submission, currentUser);
        Evaluation evaluation;
        if (existingOpt.isPresent()) {
            evaluation = existingOpt.get();
            evaluation.setScore(request.getScore());
            evaluation.setFeedback(request.getFeedback() != null ? request.getFeedback().trim() : null);
        } else {
            evaluation = Evaluation.builder()
                    .submission(submission)
                    .judge(currentUser)
                    .score(request.getScore())
                    .feedback(request.getFeedback() != null ? request.getFeedback().trim() : null)
                    .build();
        }

        evaluation = evaluationRepository.save(evaluation);
        return mapToEvaluationResponse(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsBySubmission(Long eventId, Long submissionId, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        if (submission.getRound() == null || submission.getRound().getEvent() == null ||
                !submission.getRound().getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission does not belong to this event");
        }

        // Only Admin, Coordinator, or the assigned Judges can view
        if (currentUser.getRole() == UserRole.JUDGE) {
            validateJudgeAssignment(submission, currentUser);
        } else if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.COORDINATOR) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You do not have permission to view evaluations");
        }

        List<Evaluation> evaluations = evaluationRepository.findBySubmission(submission);
        return evaluations.stream().map(this::mapToEvaluationResponse).toList();
    }

    private void validateJudgeAssignment(Submission submission, User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.COORDINATOR) {
            return; // Admins and Coordinators can evaluate any submission
        }

        if (user.getRole() != UserRole.JUDGE) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only judges can evaluate submissions");
        }

        Category category = submission.getTeam().getCategory();
        if (category == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "This team is not registered in any category yet");
        }

        boolean isAssigned = category.getJudges().stream()
                .anyMatch(j -> j.getId().equals(user.getId()));

        if (!isAssigned) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You are not assigned to grade submissions in this Category");
        }
    }

    private EvaluationResponse mapToEvaluationResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .submissionId(evaluation.getSubmission().getId())
                .judgeId(evaluation.getJudge().getId())
                .judgeName(evaluation.getJudge().getFullName())
                .score(evaluation.getScore())
                .feedback(evaluation.getFeedback())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }
}
