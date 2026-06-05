package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.SubmissionRequest;
import com.example.hackathonseal.models.dto.response.SubmissionResponse;

public interface SubmissionService {
    SubmissionResponse submit(Long eventId, Long teamId, Long roundId, SubmissionRequest request);
}

