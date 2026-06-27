package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.RoundRequest;
import com.example.hackathonseal.models.dto.response.RoundResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Round;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.RoundRepository;
import com.example.hackathonseal.services.Interface.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;

    @Override
    @Transactional
    public RoundResponse createRound(Long eventId, RoundRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (request.getSubmissionDeadline() != null) {
            if (request.getSubmissionDeadline().isBefore(event.getStartTime()) || request.getSubmissionDeadline().isAfter(event.getEndTime())) {
                throw new AppException(ErrorCode.EVENT_END_BEFORE_START, "Round submission deadline must be within the event's time range");
            }
        }

        Round round = Round.builder()
                .event(event)
                .name(request.getName())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .submissionDeadline(request.getSubmissionDeadline())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .advancementCount(request.getAdvancementCount())
                .build();

        round = roundRepository.save(round);

        return RoundResponse.builder()
                .id(round.getId())
                .eventId(event.getId())
                .name(round.getName())
                .description(round.getDescription())
                .orderIndex(round.getOrderIndex())
                .submissionDeadline(round.getSubmissionDeadline())
                .isActive(round.getIsActive())
                .advancementCount(round.getAdvancementCount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> getRoundsForEvent(Long eventId) {
        return roundRepository.findByEventIdOrderByOrderIndex(eventId)
                .stream()
                .map(r -> RoundResponse.builder()
                        .id(r.getId())
                        .eventId(r.getEvent().getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .orderIndex(r.getOrderIndex())
                        .submissionDeadline(r.getSubmissionDeadline())
                        .isActive(r.getIsActive())
                        .advancementCount(r.getAdvancementCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoundResponse updateRound(Long eventId, Long roundId, RoundRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round not found"));

        if (!round.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round does not belong to this event");
        }

        if (request.getSubmissionDeadline() != null) {
            if (request.getSubmissionDeadline().isBefore(event.getStartTime()) || request.getSubmissionDeadline().isAfter(event.getEndTime())) {
                throw new AppException(ErrorCode.EVENT_END_BEFORE_START, "Round submission deadline must be within the event's time range");
            }
        }

        round.setName(request.getName());
        round.setDescription(request.getDescription());
        round.setOrderIndex(request.getOrderIndex());
        round.setSubmissionDeadline(request.getSubmissionDeadline());
        if (request.getIsActive() != null) {
            round.setIsActive(request.getIsActive());
        }
        round.setAdvancementCount(request.getAdvancementCount());

        round = roundRepository.save(round);

        return RoundResponse.builder()
                .id(round.getId())
                .eventId(event.getId())
                .name(round.getName())
                .description(round.getDescription())
                .orderIndex(round.getOrderIndex())
                .submissionDeadline(round.getSubmissionDeadline())
                .isActive(round.getIsActive())
                .advancementCount(round.getAdvancementCount())
                .build();
    }

    @Override
    @Transactional
    public void deleteRound(Long eventId, Long roundId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round not found"));

        if (!round.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round does not belong to this event");
        }

        roundRepository.delete(round);
    }
}

