package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.response.RegistrationResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.EventRegistration;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.UserProfile;
import com.example.hackathonseal.repo.EventRegistrationRepository;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.repo.UserProfileRepository;
import com.example.hackathonseal.services.Interface.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public RegistrationResponse register(Long eventId, Long userId, User currentUser) {
        log.info("Processing event registration. Event ID: {}, User ID: {}, Requester: {}",
                eventId, userId, currentUser != null ? currentUser.getEmail() : "anonymous");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Registration failed: Event not found. Event ID: {}", eventId);
                    return new AppException(ErrorCode.EVENT_NOT_FOUND);
                });

        // capacity check
        if (event.getMaxParticipants() != null && event.getCurrentParticipants() != null
                && event.getCurrentParticipants() >= event.getMaxParticipants()) {
            log.error("Registration failed: Event capacity full. Event ID: {}, Current: {}, Max: {}",
                    eventId, event.getCurrentParticipants(), event.getMaxParticipants());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Event has reached maximum participants");
        }

        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User must be authenticated to register");
        }

        // If userId is null, default to registering the currently logged-in user (self-registration)
        Long targetUserId = userId;
        if (targetUserId == null) {
            targetUserId = currentUser.getId();
        } else if (!targetUserId.equals(currentUser.getId())) {
            // Only admin can register on behalf of other users
            if (currentUser.getRole() != com.example.hackathonseal.models.Enum.UserRole.ADMIN) {
                throw new AppException(ErrorCode.ACCESS_DENIED, "Only admins can register on behalf of other users");
            }
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    log.error("Registration failed: Target user not found. User ID: {}", userId);
                    return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                });

        if (registrationRepository.existsByEventAndUser(event, user)) {
            log.warn("Registration failed: User already registered. User ID: {}, Event ID: {}", targetUserId, eventId);
            throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "User already registered for this event");
        }

        // Fetch profile for studentCode and university to auto-fill details if needed
        String studentCode = null;
        String university = "FPT University";
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(user.getId());
        if (profileOpt.isPresent()) {
            studentCode = profileOpt.get().getStudentCode();
            university = profileOpt.get().getUniversityName();
        }
        if (university == null || university.isBlank()) {
            university = "FPT University";
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .registeredBy(currentUser)
                .registeredAt(LocalDateTime.now())
                .active(true)
                .build();

        registration = registrationRepository.save(registration);
        log.info("Registration entity saved successfully. Registration ID: {}", registration.getId());

        // increment count
        if (event.getCurrentParticipants() == null) event.setCurrentParticipants(0);
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);
        log.info("Incremented event participant count. Event ID: {}, New count: {}", eventId, event.getCurrentParticipants());

        return mapToResponse(registration, studentCode, university);
    }

    @Override
    @Transactional
    public RegistrationResponse unregister(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        EventRegistration registration = registrationRepository.findByEventAndUser(event, user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        registration.setActive(false);
        registrationRepository.save(registration);

        if (event.getCurrentParticipants() == null) event.setCurrentParticipants(0);
        event.setCurrentParticipants(Math.max(0, event.getCurrentParticipants() - 1));
        eventRepository.save(event);

        return RegistrationResponse.builder()
                .registrationId(registration.getId())
                .eventId(event.getId())
                .userId(user.getId())
                .message("Unregistered successfully")
                .build();
    }

    @Override
    public Page<RegistrationResponse> listParticipants(Long eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        return registrationRepository.findByEvent(event, pageable)
                .map(reg -> {
                    String studentCode = null;
                    String university = "FPT University";
                    Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(reg.getUser().getId());
                    if (profileOpt.isPresent()) {
                        studentCode = profileOpt.get().getStudentCode();
                        university = profileOpt.get().getUniversityName();
                    }
                    if (university == null || university.isBlank()) {
                        university = "FPT University";
                    }
                    return mapToResponse(reg, studentCode, university);
                });
    }

    private RegistrationResponse mapToResponse(EventRegistration reg, String studentCode, String university) {
        RegistrationResponse.RegistrationResponseBuilder builder = RegistrationResponse.builder()
                .registrationId(reg.getId())
                .eventId(reg.getEvent().getId())
                .userId(reg.getUser().getId())
                .guestName(reg.getUser().getFullName())
                .guestEmail(reg.getUser().getEmail())
                .guestStudentCode(studentCode)
                .guestUniversity(university)
                .message(reg.getActive() ? "ACTIVE" : "INACTIVE");

        if (reg.getRegisteredBy() != null) {
            builder.registeredByUserId(reg.getRegisteredBy().getId())
                    .registeredByUserEmail(reg.getRegisteredBy().getEmail());
        }

        if (reg.getTeam() != null) {
            builder.teamId(reg.getTeam().getId())
                    .teamName(reg.getTeam().getName());
        }

        return builder.build();
    }
}
