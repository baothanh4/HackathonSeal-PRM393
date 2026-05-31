package com.example.hackthonseal.repo;

import com.example.hackthonseal.models.entity.Event;
import com.example.hackthonseal.models.entity.EventRegistration;
import com.example.hackthonseal.models.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByEventAndUser(Event event, User user);

    Optional<EventRegistration> findByEventAndUser(Event event, User user);

    Page<EventRegistration> findByEvent(Event event, Pageable pageable);

    Page<EventRegistration> findByUser(User user, Pageable pageable);

    boolean existsByEventAndGuestEmail(Event event, String guestEmail);

    Optional<EventRegistration> findByEventAndGuestEmail(Event event, String guestEmail);

    List<EventRegistration> findByTeamAndActiveTrue(com.example.hackthonseal.models.entity.Team team);

    long countByTeamAndActiveTrue(com.example.hackthonseal.models.entity.Team team);

    Optional<EventRegistration> findByEventAndUserAndActiveTrue(Event event, User user);

    @Query("SELECT r FROM EventRegistration r WHERE r.event = :event AND r.active = true AND " +
           "(r.guestEmail = :email OR (r.user IS NOT NULL AND r.user.email = :email))")
    Optional<EventRegistration> findByEventAndEmailAndActiveTrue(@Param("event") Event event, @Param("email") String email);
}

