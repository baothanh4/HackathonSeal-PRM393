package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.response.TeamRankingResponse;
import com.example.hackathonseal.models.entity.Round;
import com.example.hackathonseal.models.entity.Team;

import java.util.List;

public interface RankingService {
    List<TeamRankingResponse> getRoundRanking(Long eventId, Long roundId, Long categoryId);
    List<TeamRankingResponse> getCategoryRanking(Long eventId, Long categoryId);
    List<TeamRankingResponse> getEventRanking(Long eventId);
    boolean isTeamEligibleForRound(Team team, Round round);
}
