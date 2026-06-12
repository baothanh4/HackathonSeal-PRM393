package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.EventCriteriaRequest;
import com.example.hackathonseal.models.dto.request.UpdateEventCriteriaRequest;
import com.example.hackathonseal.models.dto.response.EventCriteriaResponse;

import java.util.List;

public interface EventCriteriaService {
    EventCriteriaResponse addCriterionToEvent(Long eventId, EventCriteriaRequest request);
    List<EventCriteriaResponse> getCriteriaForEvent(Long eventId, boolean activeOnly);
    EventCriteriaResponse updateEventCriterion(Long eventId, Long criterionId, UpdateEventCriteriaRequest request);
    void deleteEventCriterion(Long eventId, Long criterionId);
}
