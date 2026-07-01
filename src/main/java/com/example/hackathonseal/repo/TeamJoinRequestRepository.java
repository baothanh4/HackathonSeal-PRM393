package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.EventRegistration;
import com.example.hackathonseal.models.entity.Team;
import com.example.hackathonseal.models.entity.TeamJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {
    List<TeamJoinRequest> findByTeamAndStatus(Team team, String status);
    List<TeamJoinRequest> findByRegistrationAndStatus(EventRegistration registration, String status);
    boolean existsByTeamAndRegistrationAndStatus(Team team, EventRegistration registration, String status);
}
