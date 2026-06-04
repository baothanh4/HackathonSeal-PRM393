package com.example.hackathonseal.models.dto.request;

import com.example.hackathonseal.validation.deserializer.IsoUtcLocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 300, message = "Location must be between 2 and 300 characters")
    private String location;

    @NotNull(message = "Start time is required")
    @JsonDeserialize(using = IsoUtcLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonDeserialize(using = IsoUtcLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    private String imageUrl;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 100000, message = "Max participants must not exceed 100000")
    private Integer maxParticipants;
}
