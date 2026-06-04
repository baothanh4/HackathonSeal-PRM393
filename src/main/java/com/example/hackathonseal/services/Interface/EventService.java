package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.EventRequest;
import com.example.hackathonseal.models.dto.response.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    EventResponse createEvent(EventRequest request);

    EventResponse getEventById(Long id);

    Page<EventResponse> getAllEvents(Pageable pageable);

    EventResponse updateEvent(Long id, EventRequest request);

    void deleteEvent(Long id);

    Page<EventResponse> searchEvents(String title, String status, Pageable pageable);
}
