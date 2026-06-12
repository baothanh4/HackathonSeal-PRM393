package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.EventCriteriaRequest;
import com.example.hackathonseal.models.dto.request.UpdateEventCriteriaRequest;
import com.example.hackathonseal.models.dto.response.EventCriteriaResponse;
import com.example.hackathonseal.models.entity.CriteriaTemplate;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.EventCriteria;
import com.example.hackathonseal.repo.CriteriaTemplateRepository;
import com.example.hackathonseal.repo.EventCriteriaRepository;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.services.Interface.EventCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCriteriaServiceImpl implements EventCriteriaService {

    private final EventCriteriaRepository eventCriteriaRepository;
    private final EventRepository eventRepository;
    private final CriteriaTemplateRepository criteriaTemplateRepository;

    @Override
    @Transactional
    public EventCriteriaResponse addCriterionToEvent(Long eventId, EventCriteriaRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found"));

        CriteriaTemplate template = null;
        String name = request.getCustomName();
        Double weight = request.getCustomWeight();
        Double maxScore = request.getMaxScore();

        if (request.getTemplateId() != null) {
            template = criteriaTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria template not found"));
            if (name == null || name.trim().isEmpty()) {
                name = template.getName();
            }
            if (weight == null) {
                weight = template.getDefaultWeight();
            }
            if (maxScore == null) {
                maxScore = template.getDefaultMaxScore();
            }
        }

        if (name == null || name.trim().isEmpty()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria name is required");
        }
        if (weight == null || weight <= 0) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria weight must be greater than 0");
        }
        if (maxScore == null || maxScore <= 0) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria max score must be greater than 0");
        }

        EventCriteria criterion = EventCriteria.builder()
                .event(event)
                .template(template)
                .customName(name.trim())
                .customWeight(weight)
                .maxScore(maxScore)
                .isActive(true)
                .build();

        criterion = eventCriteriaRepository.save(criterion);
        return mapToResponse(criterion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventCriteriaResponse> getCriteriaForEvent(Long eventId, boolean activeOnly) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        List<EventCriteria> criteria = activeOnly
                ? eventCriteriaRepository.findByEventIdAndIsActiveTrue(eventId)
                : eventCriteriaRepository.findByEventId(eventId);

        return criteria.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public EventCriteriaResponse updateEventCriterion(Long eventId, Long criterionId, UpdateEventCriteriaRequest request) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        EventCriteria criterion = eventCriteriaRepository.findById(criterionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event criterion not found"));

        if (!criterion.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event criterion does not belong to the specified event");
        }

        if (request.getCustomName() != null && !request.getCustomName().trim().isEmpty()) {
            criterion.setCustomName(request.getCustomName().trim());
        }
        if (request.getCustomWeight() != null) {
            if (request.getCustomWeight() <= 0) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria weight must be greater than 0");
            }
            criterion.setCustomWeight(request.getCustomWeight());
        }
        if (request.getMaxScore() != null) {
            if (request.getMaxScore() <= 0) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria max score must be greater than 0");
            }
            criterion.setMaxScore(request.getMaxScore());
        }
        if (request.getIsActive() != null) {
            criterion.setIsActive(request.getIsActive());
        }

        criterion = eventCriteriaRepository.save(criterion);
        return mapToResponse(criterion);
    }

    @Override
    @Transactional
    public void deleteEventCriterion(Long eventId, Long criterionId) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found");
        }

        EventCriteria criterion = eventCriteriaRepository.findById(criterionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event criterion not found"));

        if (!criterion.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Event criterion does not belong to the specified event");
        }

        eventCriteriaRepository.delete(criterion);
    }

    private EventCriteriaResponse mapToResponse(EventCriteria criterion) {
        return EventCriteriaResponse.builder()
                .id(criterion.getId())
                .eventId(criterion.getEvent().getId())
                .templateId(criterion.getTemplate() != null ? criterion.getTemplate().getId() : null)
                .customName(criterion.getCustomName())
                .customWeight(criterion.getCustomWeight())
                .maxScore(criterion.getMaxScore())
                .isActive(criterion.getIsActive())
                .createdAt(criterion.getCreatedAt())
                .build();
    }
}
