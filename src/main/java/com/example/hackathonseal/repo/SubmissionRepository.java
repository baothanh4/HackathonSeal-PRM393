package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTeamId(Long teamId);
    List<Submission> findByRoundId(Long roundId);
}

