package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.EventCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCriteriaRepository extends JpaRepository<EventCriteria, Long> {
    List<EventCriteria> findByEventId(Long eventId);
    List<EventCriteria> findByEventIdAndIsActiveTrue(Long eventId);
}
