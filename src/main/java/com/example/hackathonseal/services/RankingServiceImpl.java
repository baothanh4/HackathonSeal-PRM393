package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.response.TeamRankingResponse;
import com.example.hackathonseal.models.entity.*;
import com.example.hackathonseal.repo.*;
import com.example.hackathonseal.services.Interface.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final TeamRepository teamRepository;
    private final RoundRepository roundRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TeamRankingResponse> getRoundRanking(Long eventId, Long roundId, Long categoryId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round not found"));

        if (!round.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Round does not belong to this event");
        }

        List<Team> teams = teamRepository.findByEvent(event);
        if (categoryId != null) {
            teams = teams.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        List<Round> allRounds = roundRepository.findByEventIdOrderByOrderIndex(event.getId());

        return calculateRankingForRound(teams, round, allRounds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamRankingResponse> getCategoryRanking(Long eventId, Long categoryId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<Team> teams = teamRepository.findByEvent(event).stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(categoryId))
                .collect(Collectors.toList());

        List<Round> allRounds = roundRepository.findByEventIdOrderByOrderIndex(event.getId());

        return calculateOverallRanking(teams, allRounds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamRankingResponse> getEventRanking(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<Team> teams = teamRepository.findByEvent(event);
        List<Round> allRounds = roundRepository.findByEventIdOrderByOrderIndex(event.getId());

        return calculateOverallRanking(teams, allRounds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamEligibleForRound(Team team, Round round) {
        List<Round> rounds = roundRepository.findByEventIdOrderByOrderIndex(round.getEvent().getId());
        int index = rounds.indexOf(round);

        // If it's the first round, everyone is eligible
        if (index <= 0) {
            return true;
        }

        Round prevRound = rounds.get(index - 1);
        if (prevRound.getAdvancementCount() == null) {
            return true; // No advancement restriction, all advance
        }

        // Get all teams in the same category
        List<Team> siblingTeams = teamRepository.findByEvent(round.getEvent());
        if (team.getCategory() != null) {
            siblingTeams = siblingTeams.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().getId().equals(team.getCategory().getId()))
                    .collect(Collectors.toList());
        } else {
            siblingTeams = siblingTeams.stream()
                    .filter(t -> t.getCategory() == null)
                    .collect(Collectors.toList());
        }

        List<TeamRankingResponse> prevRanking = calculateRankingForRound(siblingTeams, prevRound, rounds);
        
        // Find this team's rank
        Optional<TeamRankingResponse> teamRankOpt = prevRanking.stream()
                .filter(r -> r.getTeamId().equals(team.getId()))
                .findFirst();

        if (teamRankOpt.isEmpty()) {
            return false;
        }

        return teamRankOpt.get().getRank() <= prevRound.getAdvancementCount();
    }

    private List<TeamRankingResponse> calculateRankingForRound(List<Team> teams, Round round, List<Round> allRounds) {
        List<TeamRankingResponse> rankingList = new ArrayList<>();

        for (Team team : teams) {
            double avgScore = getTeamAverageScoreInRound(team, round);

            rankingList.add(TeamRankingResponse.builder()
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .categoryId(team.getCategory() != null ? team.getCategory().getId() : null)
                    .categoryName(team.getCategory() != null ? team.getCategory().getName() : null)
                    .averageScore(avgScore)
                    .build());
        }

        // Sort descending
        rankingList.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));

        // Assign Rank (dense ranking)
        double currentScore = -1.0;
        int currentRank = 0;
        int count = 0;
        for (TeamRankingResponse item : rankingList) {
            count++;
            if (item.getAverageScore() != currentScore) {
                currentRank = count;
                currentScore = item.getAverageScore();
            }
            item.setRank(currentRank);
            
            // Determine eligibility for next round
            boolean isEligible = true;
            if (round.getAdvancementCount() != null) {
                isEligible = currentRank <= round.getAdvancementCount();
            }
            item.setIsEligibleNextRound(isEligible);
        }

        return rankingList;
    }

    private List<TeamRankingResponse> calculateOverallRanking(List<Team> teams, List<Round> allRounds) {
        List<TeamRankingResponse> rankingList = new ArrayList<>();

        for (Team team : teams) {
            double totalScore = 0.0;
            for (Round r : allRounds) {
                totalScore += getTeamAverageScoreInRound(team, r);
            }
            double overallAvg = allRounds.isEmpty() ? 0.0 : totalScore / allRounds.size();

            rankingList.add(TeamRankingResponse.builder()
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .categoryId(team.getCategory() != null ? team.getCategory().getId() : null)
                    .categoryName(team.getCategory() != null ? team.getCategory().getName() : null)
                    .averageScore(overallAvg)
                    .isEligibleNextRound(true) // Not applicable for overall ranking
                    .build());
        }

        // Sort descending
        rankingList.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));

        // Assign Rank
        double currentScore = -1.0;
        int currentRank = 0;
        int count = 0;
        for (TeamRankingResponse item : rankingList) {
            count++;
            if (item.getAverageScore() != currentScore) {
                currentRank = count;
                currentScore = item.getAverageScore();
            }
            item.setRank(currentRank);
        }

        return rankingList;
    }

    private double getTeamAverageScoreInRound(Team team, Round round) {
        List<Submission> submissions = submissionRepository.findByTeamIdAndRoundId(team.getId(), round.getId());
        if (submissions.isEmpty()) {
            return 0.0;
        }

        // Calculate average of the latest submission
        Submission latestSubmission = submissions.get(submissions.size() - 1);
        List<Evaluation> evaluations = evaluationRepository.findBySubmission(latestSubmission);
        if (evaluations.isEmpty()) {
            return 0.0;
        }

        // Group evaluations by judge
        Map<Long, List<Evaluation>> byJudge = new HashMap<>();
        for (Evaluation eval : evaluations) {
            if (eval.getJudge() != null) {
                byJudge.computeIfAbsent(eval.getJudge().getId(), k -> new ArrayList<>()).add(eval);
            }
        }

        if (byJudge.isEmpty()) {
            return 0.0;
        }

        // For each judge, sum the scores of the criteria they evaluated
        double totalSum = 0.0;
        for (List<Evaluation> judgeEvals : byJudge.values()) {
            double judgeSum = 0.0;
            for (Evaluation eval : judgeEvals) {
                if (eval.getScoreValue() != null) {
                    judgeSum += eval.getScoreValue();
                }
            }
            totalSum += judgeSum;
        }

        return totalSum / byJudge.size();
    }
}
