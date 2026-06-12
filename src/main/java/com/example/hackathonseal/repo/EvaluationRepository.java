package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Evaluation;
import com.example.hackathonseal.models.entity.Submission;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.EventCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findBySubmission(Submission submission);
    List<Evaluation> findBySubmissionAndJudge(Submission submission, User judge);
    Optional<Evaluation> findBySubmissionAndJudgeAndCriterion(Submission submission, User judge, EventCriteria criterion);
}
