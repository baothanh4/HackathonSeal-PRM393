package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.JudgeAssignmentRequest;
import com.example.hackathonseal.models.dto.response.JudgeAssignmentResponse;

import java.util.List;

public interface JudgeAssignmentService {
    JudgeAssignmentResponse assignJudge(Long eventId, JudgeAssignmentRequest request);
    List<JudgeAssignmentResponse> getAssignmentsForEvent(Long eventId);
    void deleteAssignment(Long eventId, Long assignmentId);
}
