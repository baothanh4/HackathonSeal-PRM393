package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.TeamRequest;
import com.example.hackathonseal.models.dto.response.TeamMemberResponse;
import com.example.hackathonseal.models.dto.response.TeamResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Category;
import com.example.hackathonseal.models.entity.EventRegistration;
import com.example.hackathonseal.models.entity.Team;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.UserProfile;
import com.example.hackathonseal.repo.CategoryRepository;
import com.example.hackathonseal.repo.EventRegistrationRepository;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.TeamRepository;
import com.example.hackathonseal.repo.UserProfileRepository;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.TeamService;
import com.example.hackathonseal.models.dto.response.TeamJoinRequestResponse;
import com.example.hackathonseal.models.entity.TeamJoinRequest;
import com.example.hackathonseal.repo.TeamJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(Long eventId, TeamRequest request, User currentUser) {
        log.info("Creating team. Event ID: {}, Team Name: {}, Leader: {}", eventId, request.getName(), currentUser.getEmail());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Team creation failed: Event not found. Event ID: {}", eventId);
                    return new AppException(ErrorCode.EVENT_NOT_FOUND);
                });

        if (teamRepository.existsByEventAndName(event, request.getName())) {
            log.warn("Team creation failed: Team name already exists in this event. Name: {}", request.getName());
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Team name already exists in this competition");
        }

        // Leader must be registered in the event
        EventRegistration leaderReg = registrationRepository.findByEventAndUserAndActiveTrue(event, currentUser)
                .orElseThrow(() -> {
                    log.error("Team creation failed: Creator is not registered for the event. Creator: {}", currentUser.getEmail());
                    return new AppException(ErrorCode.UNAUTHORIZED, "You must register for the event first to create a team.");
                });

        if (leaderReg.getTeam() != null) {
            log.warn("Team creation failed: Leader is already in team: {}", leaderReg.getTeam().getName());
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "You are already a member of a team in this event");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));
            if (!category.getEvent().getId().equals(event.getId())) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category does not belong to this event");
            }
        }

        Team team = Team.builder()
                .name(request.getName())
                .event(event)
                .leader(currentUser)
                .category(category)
                .build();

        team = teamRepository.save(team);

        leaderReg.setTeam(team);
        registrationRepository.save(leaderReg);

        log.info("Team created successfully. Team ID: {}, Name: {}", team.getId(), team.getName());
        return mapToTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse joinTeam(Long eventId, Long teamId, User currentUser) {
        log.info("User requesting to join team. Event ID: {}, Team ID: {}, User: {}", eventId, teamId, currentUser.getEmail());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        EventRegistration userReg = registrationRepository.findByEventAndUserAndActiveTrue(event, currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "You must register for the event first to join a team."));

        if (userReg.getTeam() != null) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "You are already a member of a team in this event");
        }

        boolean alreadyPending = teamJoinRequestRepository.existsByTeamAndRegistrationAndStatus(team, userReg, "PENDING");
        if (alreadyPending) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "You have already requested to join this team");
        }

        TeamJoinRequest request = TeamJoinRequest.builder()
                .team(team)
                .registration(userReg)
                .status("PENDING")
                .createdAt(java.time.LocalDateTime.now())
                .build();

        request = teamJoinRequestRepository.save(request);

        log.info("Join request created successfully. Request ID: {}, Team: {}, User: {}", request.getId(), team.getName(), currentUser.getEmail());
        return mapToTeamJoinRequestResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamJoinRequestResponse> getPendingJoinRequests(Long eventId, Long teamId, User currentUser) {
        log.info("Retrieving pending join requests for Team ID: {}, requested by: {}", teamId, currentUser.getEmail());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        if (!team.getLeader().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.COORDINATOR) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the team leader can view pending join requests.");
        }

        List<TeamJoinRequest> requests = teamJoinRequestRepository.findByTeamAndStatus(team, "PENDING");
        return requests.stream().map(this::mapToTeamJoinRequestResponse).toList();
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse approveJoinRequest(Long eventId, Long teamId, Long requestId, User currentUser) {
        log.info("Approving join request ID: {}, Team ID: {}, requested by: {}", requestId, teamId, currentUser.getEmail());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        if (!team.getLeader().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.COORDINATOR) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the team leader can approve join requests.");
        }

        TeamJoinRequest joinRequest = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Join request not found"));

        if (!joinRequest.getTeam().getId().equals(teamId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Join request does not belong to this team");
        }

        if (!"PENDING".equals(joinRequest.getStatus())) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Join request is not pending (current status: " + joinRequest.getStatus() + ")");
        }

        EventRegistration applicantReg = joinRequest.getRegistration();
        if (applicantReg.getTeam() != null) {
            joinRequest.setStatus("REJECTED");
            teamJoinRequestRepository.save(joinRequest);
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Applicant is already in a team");
        }

        applicantReg.setTeam(team);
        registrationRepository.save(applicantReg);

        joinRequest.setStatus("APPROVED");
        joinRequest = teamJoinRequestRepository.save(joinRequest);

        List<TeamJoinRequest> otherRequests = teamJoinRequestRepository.findByRegistrationAndStatus(applicantReg, "PENDING");
        for (TeamJoinRequest req : otherRequests) {
            req.setStatus("REJECTED");
        }
        teamJoinRequestRepository.saveAll(otherRequests);

        log.info("Join request approved successfully. Request ID: {}, Member Added: {}", requestId, applicantReg.getUser().getEmail());
        return mapToTeamJoinRequestResponse(joinRequest);
    }

    @Override
    @Transactional
    public TeamJoinRequestResponse rejectJoinRequest(Long eventId, Long teamId, Long requestId, User currentUser) {
        log.info("Rejecting join request ID: {}, Team ID: {}, requested by: {}", requestId, teamId, currentUser.getEmail());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        if (!team.getLeader().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.COORDINATOR) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the team leader can reject join requests.");
        }

        TeamJoinRequest joinRequest = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Join request not found"));

        if (!joinRequest.getTeam().getId().equals(teamId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Join request does not belong to this team");
        }

        if (!"PENDING".equals(joinRequest.getStatus())) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Join request is not pending");
        }

        joinRequest.setStatus("REJECTED");
        joinRequest = teamJoinRequestRepository.save(joinRequest);

        log.info("Join request rejected successfully. Request ID: {}", requestId);
        return mapToTeamJoinRequestResponse(joinRequest);
    }

    private TeamJoinRequestResponse mapToTeamJoinRequestResponse(TeamJoinRequest request) {
        String studentCode = null;
        String university = "FPT University";

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(request.getRegistration().getUser().getId());
        if (profileOpt.isPresent()) {
            studentCode = profileOpt.get().getStudentCode();
            university = profileOpt.get().getUniversityName();
        }
        if (university == null || university.isBlank()) {
            university = "FPT University";
        }

        return TeamJoinRequestResponse.builder()
                .requestId(request.getId())
                .teamId(request.getTeam().getId())
                .teamName(request.getTeam().getName())
                .registrationId(request.getRegistration().getId())
                .userId(request.getRegistration().getUser().getId())
                .fullName(request.getRegistration().getUser().getFullName())
                .email(request.getRegistration().getUser().getEmail())
                .studentCode(studentCode)
                .university(university)
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TeamResponse addMember(Long eventId, Long teamId, Long registrationId, String email, User currentUser) {
        log.info("Adding member to team. Event ID: {}, Team ID: {}, Target Registration ID: {}, Email: {}, Requester: {}",
                eventId, teamId, registrationId, email, currentUser.getEmail());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the team leader can add members.");
        }

        EventRegistration targetReg;
        if (registrationId != null) {
            targetReg = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Registration participant not found"));
        } else if (email != null && !email.isBlank()) {
            targetReg = registrationRepository.findByEventAndEmailAndActiveTrue(event, email.trim().toLowerCase())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Participant registration not found for the provided email: " + email));
        } else {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Either registrationId or email must be provided");
        }

        if (!targetReg.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Participant is not registered for this event");
        }

        if (Boolean.FALSE.equals(targetReg.getActive())) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Participant registration is inactive");
        }

        if (targetReg.getTeam() != null) {
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Participant is already in another team");
        }

        targetReg.setTeam(team);
        registrationRepository.save(targetReg);

        log.info("Participant added to team successfully. Team: {}, Participant Registration ID: {}", team.getName(), targetReg.getId());
        return mapToTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse removeMember(Long eventId, Long teamId, Long registrationId, String email, User currentUser) {
        log.info("Removing member from team. Event ID: {}, Team ID: {}, Target Registration ID: {}, Email: {}, Requester: {}",
                eventId, teamId, registrationId, email, currentUser.getEmail());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        // Only the team leader (or admin/coordinator) can remove members.
        if (!team.getLeader().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.COORDINATOR) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only the team leader can remove members.");
        }

        EventRegistration targetReg;
        if (registrationId != null) {
            targetReg = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Registration participant not found"));
        } else if (email != null && !email.isBlank()) {
            targetReg = registrationRepository.findByEventAndEmailAndActiveTrue(event, email.trim().toLowerCase())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Participant registration not found for the provided email: " + email));
        } else {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Either registrationId or email must be provided");
        }

        if (!team.getId().equals(targetReg.getTeam() != null ? targetReg.getTeam().getId() : null)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Participant is not in this team");
        }

        // A leader cannot remove themselves this way
        if (targetReg.getUser().getId().equals(team.getLeader().getId())) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team leader cannot be removed from the team");
        }

        targetReg.setTeam(null);
        registrationRepository.save(targetReg);

        log.info("Participant removed from team successfully. Team: {}, Participant Registration ID: {}", team.getName(), targetReg.getId());
        return mapToTeamResponse(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsInEvent(Long eventId) {
        log.info("Retrieving all teams in event ID: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<Team> teams = teamRepository.findByEvent(event);
        return teams.stream().map(this::mapToTeamResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeamDetails(Long eventId, Long teamId) {
        log.info("Retrieving team details. Event ID: {}, Team ID: {}", eventId, teamId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team not found"));

        if (!team.getEvent().getId().equals(eventId)) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Team does not belong to this event");
        }

        return mapToTeamResponse(team);
    }

    private TeamResponse mapToTeamResponse(Team team) {
        List<EventRegistration> registrations = registrationRepository.findByTeamAndActiveTrue(team);
        List<TeamMemberResponse> members = registrations.stream().map(reg -> {
            String studentCode = null;
            String university = "FPT University";

            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(reg.getUser().getId());
            if (profileOpt.isPresent()) {
                studentCode = profileOpt.get().getStudentCode();
                university = profileOpt.get().getUniversityName();
            }
            if (university == null || university.isBlank()) {
                university = "FPT University";
            }

            boolean isLeader = reg.getUser().getId().equals(team.getLeader().getId());
            return TeamMemberResponse.builder()
                    .registrationId(reg.getId())
                    .userId(reg.getUser().getId())
                    .fullName(reg.getUser().getFullName())
                    .email(reg.getUser().getEmail())
                    .studentCode(studentCode)
                    .university(university)
                    .guest(false)
                    .role(isLeader ? "LEADER" : "MEMBER")
                    .build();
        }).toList();

        return TeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .eventId(team.getEvent().getId())
                .leaderId(team.getLeader().getId())
                .leaderName(team.getLeader().getFullName())
                .categoryId(team.getCategory() != null ? team.getCategory().getId() : null)
                .categoryName(team.getCategory() != null ? team.getCategory().getName() : null)
                .memberCount(members.size())
                .members(members)
                .build();
    }
}
