package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.Enum.EventStatus;
import com.example.hackathonseal.models.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByTitleContainingIgnoreCaseAndStatus(String title, EventStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e FROM Event e JOIN Round r ON r.event = e JOIN JudgeAssignment ja ON ja.round = r WHERE ja.judge.id = :judgeId")
    java.util.List<Event> findEventsAssignedToJudge(@org.springframework.data.repository.query.Param("judgeId") Long judgeId);
}
