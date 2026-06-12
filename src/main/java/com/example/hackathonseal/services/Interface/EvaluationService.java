package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.EvaluationRequest;
import com.example.hackathonseal.models.dto.response.EvaluationResponse;
import com.example.hackathonseal.models.entity.User;

import java.util.List;

public interface EvaluationService {
    List<EvaluationResponse> evaluateSubmission(Long eventId, Long submissionId, EvaluationRequest request, User currentUser);
    List<EvaluationResponse> getEvaluationsBySubmission(Long eventId, Long submissionId, User currentUser);
}
