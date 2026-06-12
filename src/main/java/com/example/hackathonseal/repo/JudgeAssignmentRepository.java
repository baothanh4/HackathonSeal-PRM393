package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.JudgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, Long> {
    List<JudgeAssignment> findByRoundEventId(Long eventId);
    List<JudgeAssignment> findByRoundIdAndJudgeId(Long roundId, Long judgeId);
}
