package com.example.hackthonseal.validation.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class IsoUtcLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            if (value.endsWith("Z") || value.contains("+") || value.lastIndexOf('-') > value.indexOf('T')) {
                return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            }
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            try {
                return Instant.parse(value).atOffset(ZoneOffset.UTC).toLocalDateTime();
            } catch (Exception ignored) {
                return (LocalDateTime) context.handleWeirdStringValue(
                        LocalDateTime.class,
                        value,
                        "Expected ISO-8601 date-time such as 2026-05-31T12:30:24.447Z"
                );
            }
        }
    }
}

