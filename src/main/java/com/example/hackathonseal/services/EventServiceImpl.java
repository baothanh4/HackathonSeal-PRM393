package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.EventStatus;
import com.example.hackathonseal.models.dto.request.EventRequest;
import com.example.hackathonseal.models.dto.response.EventResponse;
import com.example.hackathonseal.models.dto.response.RuleResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.services.Interface.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventTimes(request);

        Event event = Event.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .location(request.getLocation().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .status(EventStatus.UPCOMING)
                .build();

        event = eventRepository.save(event);
        log.info("Event created with id: {}", event.getId());

        return mapToResponse(event);
    }

    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return mapToResponse(event);
    }

    @Override
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED);
        }

        validateEventTimes(request);

        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation().trim());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setImageUrl(request.getImageUrl());
        event.setMaxParticipants(request.getMaxParticipants());

        event = eventRepository.save(event);
        log.info("Event updated with id: {}", event.getId());

        return mapToResponse(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        eventRepository.delete(event);
        log.info("Event deleted with id: {}", id);
    }

    @Override
    public Page<EventResponse> searchEvents(String title, String status, Pageable pageable) {
        EventStatus eventStatus = null;

        if (status != null && !status.isBlank()) {
            try {
                eventStatus = EventStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid event status filter: {}", status);
                // Return empty page for invalid status
                return Page.empty(pageable);
            }
        }

        boolean hasTitle = title != null && !title.isBlank();
        boolean hasStatus = eventStatus != null;

        if (hasTitle && hasStatus) {
            return eventRepository.findByTitleContainingIgnoreCaseAndStatus(title, eventStatus, pageable)
                    .map(this::mapToResponse);
        } else if (hasTitle) {
            return eventRepository.findByTitleContainingIgnoreCase(title, pageable)
                    .map(this::mapToResponse);
        } else if (hasStatus) {
            return eventRepository.findByStatus(eventStatus, pageable)
                    .map(this::mapToResponse);
        } else {
            return eventRepository.findAll(pageable)
                    .map(this::mapToResponse);
        }
    }

    // ==================== Private Helper Methods ====================

    private void validateEventTimes(EventRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().isEqual(request.getStartTime())) {
            throw new AppException(ErrorCode.EVENT_END_BEFORE_START);
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EVENT_START_IN_PAST);
        }
    }

    private EventResponse mapToResponse(Event event) {
        java.util.List<RuleResponse> ruleResponses = null;
        if (event.getRules() != null) {
            ruleResponses = event.getRules().stream()
                    .map(rule -> RuleResponse.builder()
                            .id(rule.getId())
                            .name(rule.getName())
                            .description(rule.getDescription())
                            .eventId(event.getId())
                            .createdAt(rule.getCreatedAt())
                            .updatedAt(rule.getUpdatedAt())
                            .build())
                    .toList();
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .imageUrl(event.getImageUrl())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getCurrentParticipants())
                .status(event.getStatus().name())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .rules(ruleResponses)
                .build();
    }
}
