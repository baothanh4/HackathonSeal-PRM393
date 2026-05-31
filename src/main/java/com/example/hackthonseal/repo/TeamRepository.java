package com.example.hackthonseal.repo;

import com.example.hackthonseal.models.entity.Event;
import com.example.hackthonseal.models.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByEvent(Event event);
    boolean existsByEventAndName(Event event, String name);
}
