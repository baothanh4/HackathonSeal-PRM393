package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.SubmissionStatus;
import com.example.hackathonseal.models.dto.request.SubmissionRequest;
import com.example.hackathonseal.models.dto.response.SubmissionResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Round;
import com.example.hackathonseal.models.entity.Submission;
import com.example.hackathonseal.models.entity.Team;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.RoundRepository;
import com.example.hackathonseal.repo.SubmissionRepository;
import com.example.hackathonseal.repo.TeamRepository;
import com.example.hackathonseal.services.Interface.SubmissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final RoundRepository roundRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public SubmissionResponse submit(Long eventId, Long teamId, Long roundId, SubmissionRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (team.getEvent() == null || !team.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Team does not belong to event");
        }

        Round round = roundRepository.findById(roundId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (round.getEvent() == null || !round.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round does not belong to event");
        }

        // check permission: only team leader or admin can submit
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = (User) principal;

        boolean isLeader = team.getLeader() != null && team.getLeader().getId().equals(user.getId());
        boolean isAdmin = user.getRole() != null && user.getRole().name().equals("ADMIN");
        if (!isLeader && !isAdmin) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // validate github url
        String githubUrl = request.getGithubUrl().trim();
        if (!githubUrl.startsWith("http" ) || !githubUrl.contains("github.com")) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Invalid GitHub URL");
        }

        // determine status based on deadline
        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        LocalDateTime now = LocalDateTime.now();
        if (round.getSubmissionDeadline() != null && now.isAfter(round.getSubmissionDeadline())) {
            status = SubmissionStatus.LATE;
        }

        Submission submission = Submission.builder()
                .team(team)
                .round(round)
                .projectName(request.getProjectName().trim())
                .githubUrl(githubUrl)
                .versionNumber(1)
                .status(status)
                .build();

        submission = submissionRepository.save(submission);

        return SubmissionResponse.builder()
                .id(submission.getId())
                .teamId(team.getId())
                .roundId(round.getId())
                .projectName(submission.getProjectName())
                .githubUrl(submission.getGithubUrl())
                .status(submission.getStatus() != null ? submission.getStatus().name() : null)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}

