package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.TeamRequest;
import com.example.hackathonseal.models.dto.response.TeamResponse;
import com.example.hackathonseal.models.dto.response.TeamJoinRequestResponse;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.services.Interface.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Endpoints for managing competition teams within events")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "Create a new team for a competition/event")
    public ResponseEntity<TeamResponse> createTeam(
            @PathVariable Long eventId,
            @Valid @RequestBody TeamRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamResponse response = teamService.createTeam(eventId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{teamId}/join")
    @Operation(summary = "Join an existing team in a competition/event")
    public ResponseEntity<TeamJoinRequestResponse> joinTeam(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamJoinRequestResponse response = teamService.joinTeam(eventId, teamId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/join-requests")
    @Operation(summary = "Get list of all pending join requests for the team (Leader only)")
    public ResponseEntity<List<TeamJoinRequestResponse>> getPendingJoinRequests(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TeamJoinRequestResponse> response = teamService.getPendingJoinRequests(eventId, teamId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{teamId}/join-requests/{requestId}/approve")
    @Operation(summary = "Approve a team join request (Leader only)")
    public ResponseEntity<TeamJoinRequestResponse> approveJoinRequest(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamJoinRequestResponse response = teamService.approveJoinRequest(eventId, teamId, requestId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{teamId}/join-requests/{requestId}/reject")
    @Operation(summary = "Reject a team join request (Leader only)")
    public ResponseEntity<TeamJoinRequestResponse> rejectJoinRequest(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamJoinRequestResponse response = teamService.rejectJoinRequest(eventId, teamId, requestId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{teamId}/add-member")
    @Operation(summary = "Add another participant (user or guest) to the team (Leader only)")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @RequestParam(required = false) Long registrationId,
            @RequestParam(required = false) String email,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamResponse response = teamService.addMember(eventId, teamId, registrationId, email, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{teamId}/remove-member")
    @Operation(summary = "Remove a participant from the team (Leader or Admin/Coordinator only)")
    public ResponseEntity<TeamResponse> removeMember(
            @PathVariable Long eventId,
            @PathVariable Long teamId,
            @RequestParam(required = false) Long registrationId,
            @RequestParam(required = false) String email,
            @AuthenticationPrincipal User currentUser
    ) {
        TeamResponse response = teamService.removeMember(eventId, teamId, registrationId, email, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get list of all teams in a competition/event")
    public ResponseEntity<List<TeamResponse>> getTeamsInEvent(
            @PathVariable Long eventId
    ) {
        List<TeamResponse> response = teamService.getTeamsInEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get team details and members list")
    public ResponseEntity<TeamResponse> getTeamDetails(
            @PathVariable Long eventId,
            @PathVariable Long teamId
    ) {
        TeamResponse response = teamService.getTeamDetails(eventId, teamId);
        return ResponseEntity.ok(response);
    }
}
