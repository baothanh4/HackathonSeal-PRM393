package com.example.hackathonseal.config;

import com.example.hackathonseal.models.Enum.*;
import com.example.hackathonseal.models.entity.*;
import com.example.hackathonseal.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RoundRepository roundRepository;
    private final EventCriteriaRepository eventCriteriaRepository;
    private final TeamRepository teamRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@example.com").isPresent()) {
            log.info("Test data already seeded. Skipping seeder.");
            return;
        }

        log.info("Seeding test data for ranking and advancement flow...");

        // 1. Create Users
        String adminPassword = "Admin" + "@" + "123";
        String judgePassword = "Judge" + "@" + "123";
        String studentPassword = "Student" + "@" + "123";

        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode(adminPassword))
                .fullName("System Administrator")
                .role(UserRole.ADMIN)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        User judge1 = User.builder()
                .email("judge1@example.com")
                .password(passwordEncoder.encode(judgePassword))
                .fullName("Guest Judge One")
                .role(UserRole.JUDGE)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        User judge2 = User.builder()
                .email("judge2@example.com")
                .password(passwordEncoder.encode(judgePassword))
                .fullName("Guest Judge Two")
                .role(UserRole.JUDGE)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        User studentA = User.builder()
                .email("studenta@example.com")
                .password(passwordEncoder.encode(studentPassword))
                .fullName("Student Leader A")
                .role(UserRole.STUDENT)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        User studentB = User.builder()
                .email("studentb@example.com")
                .password(passwordEncoder.encode(studentPassword))
                .fullName("Student Leader B")
                .role(UserRole.STUDENT)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.saveAll(Arrays.asList(admin, judge1, judge2, studentA, studentB));

        // 2. Create Event
        Event event = Event.builder()
                .title("Hackathon Seal 2026")
                .description("National Software Engineering Competition")
                .location("FPT University Campus")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .maxParticipants(100)
                .currentParticipants(2)
                .status(EventStatus.UPCOMING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);

        // 3. Register students to Event
        EventRegistration regA = EventRegistration.builder()
                .event(event)
                .user(studentA)
                .registeredAt(LocalDateTime.now())
                .active(true)
                .build();

        EventRegistration regB = EventRegistration.builder()
                .event(event)
                .user(studentB)
                .registeredAt(LocalDateTime.now())
                .active(true)
                .build();

        eventRegistrationRepository.saveAll(Arrays.asList(regA, regB));

        // 4. Create Category & Assign Judges
        Category category = Category.builder()
                .name("Software Engineering")
                .description("Web, Mobile and Cloud Projects")
                .event(event)
                .judges(new ArrayList<>(Arrays.asList(judge1, judge2)))
                .build();

        category = categoryRepository.save(category);

        // 5. Create Rounds
        Round round1 = Round.builder()
                .event(event)
                .name("Round 1: Elimination")
                .description("Only the top team advances")
                .orderIndex(1)
                .submissionDeadline(LocalDateTime.now().plusHours(12))
                .isActive(true)
                .advancementCount(1)
                .build();

        Round round2 = Round.builder()
                .event(event)
                .name("Round 2: Final")
                .description("Final presentation and demo")
                .orderIndex(2)
                .submissionDeadline(LocalDateTime.now().plusHours(24))
                .isActive(true)
                .advancementCount(null)
                .build();

        roundRepository.saveAll(Arrays.asList(round1, round2));

        // 6. Create Event Criteria
        EventCriteria criteria = EventCriteria.builder()
                .event(event)
                .customName("Code Quality & Implementation")
                .customWeight(1.0)
                .maxScore(100.0)
                .isActive(true)
                .build();

        criteria = eventCriteriaRepository.save(criteria);

        // 7. Assign Judges to Rounds for this Category
        JudgeAssignment assignR1J1 = JudgeAssignment.builder()
                .judge(judge1)
                .round(round1)
                .category(category)
                .judgeType(JudgeType.GUEST)
                .build();

        JudgeAssignment assignR1J2 = JudgeAssignment.builder()
                .judge(judge2)
                .round(round1)
                .category(category)
                .judgeType(JudgeType.GUEST)
                .build();

        JudgeAssignment assignR2J1 = JudgeAssignment.builder()
                .judge(judge1)
                .round(round2)
                .category(category)
                .judgeType(JudgeType.GUEST)
                .build();

        JudgeAssignment assignR2J2 = JudgeAssignment.builder()
                .judge(judge2)
                .round(round2)
                .category(category)
                .judgeType(JudgeType.GUEST)
                .build();

        judgeAssignmentRepository.saveAll(Arrays.asList(assignR1J1, assignR1J2, assignR2J1, assignR2J2));

        // 8. Create Teams
        Team teamA = Team.builder()
                .name("Team Alpha")
                .event(event)
                .leader(studentA)
                .category(category)
                .build();

        Team teamB = Team.builder()
                .name("Team Beta")
                .event(event)
                .leader(studentB)
                .category(category)
                .build();

        teamRepository.saveAll(Arrays.asList(teamA, teamB));

        // Update registrations with team
        regA.setTeam(teamA);
        regB.setTeam(teamB);
        eventRegistrationRepository.saveAll(Arrays.asList(regA, regB));

        // 9. Submissions in Round 1
        Submission subA = Submission.builder()
                .team(teamA)
                .round(round1)
                .projectName("Alpha Portal")
                .githubUrl("https://github.com/alpha/portal")
                .versionNumber(1)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        Submission subB = Submission.builder()
                .team(teamB)
                .round(round1)
                .projectName("Beta Tracker")
                .githubUrl("https://github.com/beta/tracker")
                .versionNumber(1)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        submissionRepository.saveAll(Arrays.asList(subA, subB));

        // 10. Evaluations in Round 1
        // Team A (Average: 91.0)
        Evaluation evalA1 = Evaluation.builder()
                .submission(subA)
                .judge(judge1)
                .criterion(criteria)
                .scoreValue(90.0)
                .feedback("Excellent backend codebase.")
                .build();

        Evaluation evalA2 = Evaluation.builder()
                .submission(subA)
                .judge(judge2)
                .criterion(criteria)
                .scoreValue(92.0)
                .feedback("Great architecture and clean code.")
                .build();

        // Team B (Average: 81.0)
        Evaluation evalB1 = Evaluation.builder()
                .submission(subB)
                .judge(judge1)
                .criterion(criteria)
                .scoreValue(80.0)
                .feedback("Good features but lacks tests.")
                .build();

        Evaluation evalB2 = Evaluation.builder()
                .submission(subB)
                .judge(judge2)
                .criterion(criteria)
                .scoreValue(82.0)
                .feedback("Clean UI, but code is redundant in some parts.")
                .build();

        evaluationRepository.saveAll(Arrays.asList(evalA1, evalA2, evalB1, evalB2));

        log.info("========================================= TEST DATA SEEDED SUCCESSFULLY =========================================");
        log.info("Credentials available for testing:");
        log.info("- Admin: admin@example.com / " + adminPassword);
        log.info("- Judge 1: judge1@example.com / " + judgePassword);
        log.info("- Judge 2: judge2@example.com / " + judgePassword);
        log.info("- Team A Leader: studenta@example.com / " + studentPassword + "  (Score: 91.0 -> Rank 1 - Advanced)");
        log.info("- Team B Leader: studentb@example.com / " + studentPassword + "  (Score: 81.0 -> Rank 2 - Eliminated)");
        log.info("=================================================================================================================");
    }
}
