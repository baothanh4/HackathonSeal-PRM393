package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.CriterionScoreRequest;
import com.example.hackathonseal.models.dto.request.EvaluationRequest;
import com.example.hackathonseal.models.dto.response.EvaluationResponse;
import com.example.hackathonseal.models.entity.*;
import com.example.hackathonseal.repo.EvaluationRepository;
import com.example.hackathonseal.repo.EventCriteriaRepository;
import com.example.hackathonseal.repo.JudgeAssignmentRepository;
import com.example.hackathonseal.repo.SubmissionRepository;
import com.example.hackathonseal.services.Interface.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final EventCriteriaRepository eventCriteriaRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;

    @Override
    @Transactional
    public List<EvaluationResponse> evaluateSubmission(Long eventId, Long submissionId, EvaluationRequest request, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        if (submission.getRound() == null || submission.getRound().getEvent() == null ||
                !submission.getRound().getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission does not belong to this event");
        }

        // Validate Judge permissions
        validateJudgeAssignment(submission, currentUser);

        List<EvaluationResponse> responses = new ArrayList<>();

        for (CriterionScoreRequest scoreReq : request.getScores()) {
            EventCriteria criterion = eventCriteriaRepository.findById(scoreReq.getCriterionId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event criterion not found"));

            if (!criterion.getEvent().getId().equals(eventId)) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criterion does not belong to this event");
            }
            if (!criterion.getIsActive()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criterion is not active");
            }

            if (scoreReq.getScoreValue() < 0 || scoreReq.getScoreValue() > criterion.getMaxScore()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Score value must be between 0 and " + criterion.getMaxScore());
            }

            Optional<Evaluation> existingOpt = evaluationRepository.findBySubmissionAndJudgeAndCriterion(submission, currentUser, criterion);
            Evaluation evaluation;
            if (existingOpt.isPresent()) {
                evaluation = existingOpt.get();
                evaluation.setScoreValue(scoreReq.getScoreValue());
                evaluation.setFeedback(scoreReq.getFeedback() != null ? scoreReq.getFeedback().trim() : null);
                evaluation.setInternalNote(scoreReq.getInternalNote() != null ? scoreReq.getInternalNote().trim() : null);
                evaluation.setIsCalibration(scoreReq.getIsCalibration() != null ? scoreReq.getIsCalibration() : false);
                evaluation.setIsFinalized(scoreReq.getIsFinalized() != null ? scoreReq.getIsFinalized() : false);
            } else {
                evaluation = Evaluation.builder()
                        .submission(submission)
                        .judge(currentUser)
                        .criterion(criterion)
                        .scoreValue(scoreReq.getScoreValue())
                        .feedback(scoreReq.getFeedback() != null ? scoreReq.getFeedback().trim() : null)
                        .internalNote(scoreReq.getInternalNote() != null ? scoreReq.getInternalNote().trim() : null)
                        .isCalibration(scoreReq.getIsCalibration() != null ? scoreReq.getIsCalibration() : false)
                        .isFinalized(scoreReq.getIsFinalized() != null ? scoreReq.getIsFinalized() : false)
                        .build();
            }

            evaluation = evaluationRepository.save(evaluation);
            responses.add(mapToEvaluationResponse(evaluation));
        }

        return responses;
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

        List<Evaluation> evaluations;

        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.COORDINATOR) {
            evaluations = evaluationRepository.findBySubmission(submission);
        } else if (currentUser.getRole() == UserRole.JUDGE) {
            validateJudgeAssignment(submission, currentUser);
            evaluations = evaluationRepository.findBySubmissionAndJudge(submission, currentUser);
        } else {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You do not have permission to view evaluations");
        }

        return evaluations.stream().map(this::mapToEvaluationResponse).toList();
    }

    private void validateJudgeAssignment(Submission submission, User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.COORDINATOR) {
            return; // Admins and Coordinators can evaluate any submission
        }

        if (user.getRole() != UserRole.JUDGE) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only judges can evaluate submissions");
        }

        Round round = submission.getRound();
        if (round == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Submission is not associated with any round");
        }

        Category category = submission.getTeam().getCategory();
        if (category == null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "This team is not registered in any category yet");
        }

        List<JudgeAssignment> assignments = judgeAssignmentRepository.findByRoundIdAndJudgeId(round.getId(), user.getId());

        boolean isAssigned = assignments.stream().anyMatch(a ->
                a.getCategory() == null || a.getCategory().getId().equals(category.getId())
        );

        if (!isAssigned) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You are not assigned to grade submissions in this Round / Category");
        }
    }

    private EvaluationResponse mapToEvaluationResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .submissionId(evaluation.getSubmission().getId())
                .judgeId(evaluation.getJudge().getId())
                .judgeName(evaluation.getJudge().getFullName())
                .criterionId(evaluation.getCriterion().getId())
                .criterionName(evaluation.getCriterion().getCustomName())
                .scoreValue(evaluation.getScoreValue())
                .feedback(evaluation.getFeedback())
                .internalNote(evaluation.getInternalNote())
                .isCalibration(evaluation.getIsCalibration())
                .isFinalized(evaluation.getIsFinalized())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }
}
