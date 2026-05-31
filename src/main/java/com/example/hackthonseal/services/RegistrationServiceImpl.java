package com.example.hackthonseal.services;

import com.example.hackthonseal.exception.AppException;
import com.example.hackthonseal.models.Enum.ErrorCode;
import com.example.hackthonseal.models.dto.response.RegistrationResponse;
import com.example.hackthonseal.models.entity.Event;
import com.example.hackthonseal.models.entity.EventRegistration;
import com.example.hackthonseal.models.entity.User;
import com.example.hackthonseal.models.entity.UserProfile;
import com.example.hackthonseal.repo.EventRegistrationRepository;
import com.example.hackthonseal.repo.EventRepository;
import com.example.hackthonseal.repo.UserRepository;
import com.example.hackthonseal.repo.UserProfileRepository;
import com.example.hackthonseal.services.Interface.RegistrationService;
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
    public RegistrationResponse register(Long eventId, Long userId, String guestName, String guestEmail, String guestStudentCode, String guestUniversity, User currentUser) {
        log.info("Processing event registration. Event ID: {}, User ID: {}, Guest Email: {}, Requester: {}",
                eventId, userId, guestEmail, currentUser != null ? currentUser.getEmail() : "anonymous");

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

        EventRegistration registration;

        if (userId != null) {
            log.info("Registering user. User ID: {}, Event ID: {}", userId, eventId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Registration failed: Target user not found. User ID: {}", userId);
                        return new AppException(ErrorCode.RESOURCE_NOT_FOUND);
                    });

            if (registrationRepository.existsByEventAndUser(event, user)) {
                log.warn("Registration failed: User already registered. User ID: {}, Event ID: {}", userId, eventId);
                throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "User already registered for this event");
            }

            // Fetch profile for studentCode and university to auto-fill details
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

            registration = EventRegistration.builder()
                    .event(event)
                    .user(user)
                    .guestName(user.getFullName())
                    .guestEmail(user.getEmail())
                    .guestStudentCode(studentCode)
                    .guestUniversity(university)
                    .registeredBy(currentUser)
                    .registeredAt(LocalDateTime.now())
                    .active(true)
                    .build();

        } else {
            log.info("Registering guest. Guest Email: {}, Event ID: {}", guestEmail, eventId);
            // guest registration: guestEmail is required
            if (guestEmail == null || guestEmail.isBlank()) {
                log.warn("Registration failed: Guest email is blank");
                throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT, "Guest email is required");
            }

            if (userRepository.existsByEmail(guestEmail)) {
                log.warn("Registration failed: Email belongs to registered user. Email: {}", guestEmail);
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email belongs to an existing user. Please register using their account.");
            }

            if (registrationRepository.existsByEventAndGuestEmail(event, guestEmail)) {
                log.warn("Registration failed: Guest already registered. Guest Email: {}, Event ID: {}", guestEmail, eventId);
                throw new AppException(ErrorCode.EVENT_ALREADY_CANCELLED, "Guest already registered for this event");
            }

            registration = EventRegistration.builder()
                    .event(event)
                    .user(null)
                    .guestName(guestName)
                    .guestEmail(guestEmail)
                    .guestStudentCode(guestStudentCode)
                    .guestUniversity(guestUniversity)
                    .registeredBy(currentUser)
                    .registeredAt(LocalDateTime.now())
                    .active(true)
                    .build();
        }

        registration = registrationRepository.save(registration);
        log.info("Registration entity saved successfully. Registration ID: {}", registration.getId());

        // increment count
        if (event.getCurrentParticipants() == null) event.setCurrentParticipants(0);
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);
        log.info("Incremented event participant count. Event ID: {}, New count: {}", eventId, event.getCurrentParticipants());

        RegistrationResponse.RegistrationResponseBuilder builder = RegistrationResponse.builder()
                .registrationId(registration.getId())
                .eventId(event.getId())
                .message("Registered successfully");

        if (registration.getUser() != null) {
            builder.userId(registration.getUser().getId());
        }

        builder.guestName(registration.getGuestName())
                .guestEmail(registration.getGuestEmail())
                .guestStudentCode(registration.getGuestStudentCode())
                .guestUniversity(registration.getGuestUniversity());

        if (registration.getRegisteredBy() != null) {
            builder.registeredByUserId(registration.getRegisteredBy().getId())
                    .registeredByUserEmail(registration.getRegisteredBy().getEmail());
        }

        if (registration.getTeam() != null) {
            builder.teamId(registration.getTeam().getId())
                    .teamName(registration.getTeam().getName());
        }

        return builder.build();
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
                    RegistrationResponse.RegistrationResponseBuilder builder = RegistrationResponse.builder()
                            .registrationId(reg.getId())
                            .eventId(reg.getEvent().getId())
                            .message(reg.getActive() ? "ACTIVE" : "INACTIVE");

                    if (reg.getUser() != null) {
                        builder.userId(reg.getUser().getId());
                    }

                    String guestName = reg.getGuestName() != null ? reg.getGuestName() : (reg.getUser() != null ? reg.getUser().getFullName() : null);
                    String guestEmail = reg.getGuestEmail() != null ? reg.getGuestEmail() : (reg.getUser() != null ? reg.getUser().getEmail() : null);
                    
                    String studentCode = reg.getGuestStudentCode();
                    String university = reg.getGuestUniversity();
                    if (studentCode == null && reg.getUser() != null) {
                        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(reg.getUser().getId());
                        if (profileOpt.isPresent()) {
                            studentCode = profileOpt.get().getStudentCode();
                            university = profileOpt.get().getUniversityName();
                        }
                    }
                    if (university == null && reg.getUser() != null) {
                        university = "FPT University";
                    }

                    builder.guestName(guestName)
                            .guestEmail(guestEmail)
                            .guestStudentCode(studentCode)
                            .guestUniversity(university);

                    if (reg.getRegisteredBy() != null) {
                        builder.registeredByUserId(reg.getRegisteredBy().getId())
                                .registeredByUserEmail(reg.getRegisteredBy().getEmail());
                    }

                    if (reg.getTeam() != null) {
                        builder.teamId(reg.getTeam().getId())
                                .teamName(reg.getTeam().getName());
                    }

                    return builder.build();
                });
    }
}

