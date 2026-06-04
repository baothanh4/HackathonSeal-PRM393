package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByEventId(Long eventId);
}
