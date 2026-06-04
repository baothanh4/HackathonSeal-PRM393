package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.TeamRequest;
import com.example.hackathonseal.models.dto.response.TeamResponse;
import com.example.hackathonseal.models.entity.User;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(Long eventId, TeamRequest request, User currentUser);
    TeamResponse joinTeam(Long eventId, Long teamId, User currentUser);
    TeamResponse addMember(Long eventId, Long teamId, Long registrationId, String email, User currentUser);
    List<TeamResponse> getTeamsInEvent(Long eventId);
    TeamResponse getTeamDetails(Long eventId, Long teamId);
}
