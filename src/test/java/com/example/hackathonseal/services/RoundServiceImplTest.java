package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.RoundRequest;
import com.example.hackathonseal.models.dto.response.RoundResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Round;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.RoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoundServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private RoundServiceImpl roundService;

    private Event event;
    private RoundRequest validRequest;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        validRequest = new RoundRequest();
        validRequest.setName("Round 1");
        validRequest.setDescription("Elimination");
        validRequest.setOrderIndex(1);
        validRequest.setSubmissionDeadline(event.getStartTime().plusDays(1));
        validRequest.setIsActive(true);
        validRequest.setAdvancementCount(5);
    }

    @Test
    void createRound_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        RoundResponse response = roundService.createRound(1L, validRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Round 1", response.getName());
        assertEquals(validRequest.getSubmissionDeadline(), response.getSubmissionDeadline());
    }

    @Test
    void createRound_DeadlineAfterEventEnd_ThrowsException() {
        validRequest.setSubmissionDeadline(event.getEndTime().plusHours(1));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        AppException exception = assertThrows(AppException.class, () ->
                roundService.createRound(1L, validRequest)
        );

        assertEquals(ErrorCode.EVENT_END_BEFORE_START.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Round submission deadline must be within the event's time range"));
    }

    @Test
    void createRound_DeadlineBeforeEventStart_ThrowsException() {
        validRequest.setSubmissionDeadline(event.getStartTime().minusHours(1));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        AppException exception = assertThrows(AppException.class, () ->
                roundService.createRound(1L, validRequest)
        );

        assertEquals(ErrorCode.EVENT_END_BEFORE_START.getCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Round submission deadline must be within the event's time range"));
    }

    @Test
    void updateRound_Success() {
        Round existingRound = Round.builder()
                .id(10L)
                .event(event)
                .name("Old Name")
                .submissionDeadline(event.getStartTime().plusDays(2))
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(roundRepository.findById(10L)).thenReturn(Optional.of(existingRound));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoundResponse response = roundService.updateRound(1L, 10L, validRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Round 1", response.getName());
    }

    @Test
    void updateRound_RoundNotBelongToEvent_ThrowsException() {
        Event otherEvent = Event.builder().id(2L).build();
        Round existingRound = Round.builder()
                .id(10L)
                .event(otherEvent)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(roundRepository.findById(10L)).thenReturn(Optional.of(existingRound));

        AppException exception = assertThrows(AppException.class, () ->
                roundService.updateRound(1L, 10L, validRequest)
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    void deleteRound_Success() {
        Round existingRound = Round.builder()
                .id(10L)
                .event(event)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(roundRepository.findById(10L)).thenReturn(Optional.of(existingRound));
        doNothing().when(roundRepository).delete(existingRound);

        assertDoesNotThrow(() -> roundService.deleteRound(1L, 10L));
        verify(roundRepository, times(1)).delete(existingRound);
    }
}
