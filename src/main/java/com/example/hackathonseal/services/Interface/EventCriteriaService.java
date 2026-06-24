package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.EventCriteriaRequest;
import com.example.hackathonseal.models.dto.request.UpdateEventCriteriaRequest;
import com.example.hackathonseal.models.dto.response.EventCriteriaResponse;

import java.util.List;

public interface EventCriteriaService {
    EventCriteriaResponse addCriterionToRound(Long roundId, EventCriteriaRequest request);
    List<EventCriteriaResponse> getCriteriaForRound(Long roundId, boolean activeOnly);
    EventCriteriaResponse updateEventCriterion(Long roundId, Long criterionId, UpdateEventCriteriaRequest request);
    void deleteEventCriterion(Long roundId, Long criterionId);
}
