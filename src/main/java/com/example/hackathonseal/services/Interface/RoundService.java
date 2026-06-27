package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.RoundRequest;
import com.example.hackathonseal.models.dto.response.RoundResponse;

import java.util.List;

public interface RoundService {
    RoundResponse createRound(Long eventId, RoundRequest request);
    List<RoundResponse> getRoundsForEvent(Long eventId);
    RoundResponse updateRound(Long eventId, Long roundId, RoundRequest request);
    void deleteRound(Long eventId, Long roundId);
}

