package com.example.hackathonseal.models.Enum;

public enum ErrorCode {
    // Auth Errors
    INVALID_EMAIL_OR_PASSWORD("401", "Invalid email or password"),
    ACCOUNT_NOT_APPROVED("403", "Account not approved"),
    EMAIL_ALREADY_EXISTS("409", "Email already exists"),
    INVALID_EMAIL_FORMAT("400", "Invalid email format"),
    PASSWORD_TOO_SHORT("400", "Password must be at least 8 characters"),
    PASSWORD_WEAK("400", "Password must contain uppercase, lowercase, numbers and special characters"),
    FULL_NAME_REQUIRED("400", "Full name is required"),
    FULL_NAME_INVALID("400", "Full name must be between 2 and 100 characters"),

    // Participant Type Errors
    PARTICIPANT_TYPE_REQUIRED("400", "Participant type is required"),

    // External Student Errors
    UNIVERSITY_NAME_REQUIRED("400", "University name is required for external students"),
    UNIVERSITY_NAME_INVALID("400", "University name must be between 2 and 100 characters"),
    STUDENT_CODE_REQUIRED("400", "Student code is required for external students"),
    STUDENT_CODE_INVALID("400", "Student code is invalid"),
    STUDENT_CODE_ALREADY_EXISTS("400", "Student code already exists"),

    // Event Errors
    EVENT_NOT_FOUND("404", "Event not found"),
    EVENT_ALREADY_CANCELLED("400", "Event is already cancelled"),
    EVENT_END_BEFORE_START("400", "End time must be after start time"),
    EVENT_START_IN_PAST("400", "Start time must be in the future"),
    RULE_NOT_FOUND("404", "Rule not found"),

    // General Errors
    INTERNAL_SERVER_ERROR("500", "Internal server error"),
    RESOURCE_NOT_FOUND("404", "Resource not found"),
    UNAUTHORIZED("401", "Unauthorized"),
    ACCESS_DENIED("403", "Access denied");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

