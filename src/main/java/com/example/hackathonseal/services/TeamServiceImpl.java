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
import com.example.hackathonseal.services.Interface.TeamService;
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
    public TeamResponse joinTeam(Long eventId, Long teamId, User currentUser) {
        log.info("User joining team. Event ID: {}, Team ID: {}, User: {}", eventId, teamId, currentUser.getEmail());

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

        userReg.setTeam(team);
        registrationRepository.save(userReg);

        log.info("User joined team successfully. Team: {}, User: {}", team.getName(), currentUser.getEmail());
        return mapToTeamResponse(team);
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

            return TeamMemberResponse.builder()
                    .registrationId(reg.getId())
                    .userId(reg.getUser().getId())
                    .fullName(reg.getUser().getFullName())
                    .email(reg.getUser().getEmail())
                    .studentCode(studentCode)
                    .university(university)
                    .guest(false)
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
