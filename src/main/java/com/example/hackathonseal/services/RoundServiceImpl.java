package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.dto.request.RoundRequest;
import com.example.hackathonseal.models.dto.response.RoundResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Round;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.RoundRepository;
import com.example.hackathonseal.services.Interface.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;

    @Override
    public RoundResponse createRound(Long eventId, RoundRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(com.example.hackathonseal.models.Enum.ErrorCode.EVENT_NOT_FOUND));

        Round round = Round.builder()
                .event(event)
                .name(request.getName())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .submissionDeadline(request.getSubmissionDeadline())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
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
                .build();
    }

    @Override
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
                        .build())
                .collect(Collectors.toList());
    }
}

