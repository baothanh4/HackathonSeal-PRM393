package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    List<Team> findByEvent(Event event);
    boolean existsByEventAndName(Event event, String name);
}
