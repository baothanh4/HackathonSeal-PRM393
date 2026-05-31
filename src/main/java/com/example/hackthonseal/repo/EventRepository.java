package com.example.hackthonseal.repo;

import com.example.hackthonseal.models.Enum.EventStatus;
import com.example.hackthonseal.models.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByTitleContainingIgnoreCaseAndStatus(String title, EventStatus status, Pageable pageable);
}
