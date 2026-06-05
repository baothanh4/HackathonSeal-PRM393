package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByEventIdOrderByOrderIndex(Long eventId);
}

