package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.TeamRequest;
import com.example.hackathonseal.models.dto.response.TeamResponse;
import com.example.hackathonseal.models.entity.User;

import com.example.hackathonseal.models.dto.response.TeamJoinRequestResponse;

import java.util.List;

public interface TeamService {
    TeamResponse createTeam(Long eventId, TeamRequest request, User currentUser);
    TeamJoinRequestResponse joinTeam(Long eventId, Long teamId, User currentUser);
    TeamResponse addMember(Long eventId, Long teamId, Long registrationId, String email, User currentUser);
    TeamResponse removeMember(Long eventId, Long teamId, Long registrationId, String email, User currentUser);
    List<TeamResponse> getTeamsInEvent(Long eventId);
    TeamResponse getTeamDetails(Long eventId, Long teamId);
    List<TeamJoinRequestResponse> getPendingJoinRequests(Long eventId, Long teamId, User currentUser);
    TeamJoinRequestResponse approveJoinRequest(Long eventId, Long teamId, Long requestId, User currentUser);
    TeamJoinRequestResponse rejectJoinRequest(Long eventId, Long teamId, Long requestId, User currentUser);
}
