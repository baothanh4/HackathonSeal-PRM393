package com.example.hackthonseal.exception;

import com.example.hackthonseal.models.Enum.ErrorCode;
import com.example.hackthonseal.models.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        log.error("AppException: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(getHttpStatus(ex.getErrorCode()))
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Map error codes to appropriate HTTP status codes
     * 
     * HTTP Status Codes:
     * - 400 Bad Request: Invalid input, validation errors
     * - 401 Unauthorized: Authentication failed
     * - 403 Forbidden: User doesn't have permission
     * - 404 Not Found: Resource not found
     * - 409 Conflict: Email/Student Code already exists
     * - 500 Internal Server Error: Unexpected errors
     */
    private HttpStatus getHttpStatus(String errorCode) {
        return switch (errorCode) {
            // 401 Unauthorized - Authentication errors
            case "401" -> HttpStatus.UNAUTHORIZED;  // Invalid email or password
            case "403" -> HttpStatus.FORBIDDEN;     // Account not approved
             // Unauthorized
            
            // 409 Conflict - Resource already exists
            case "409" -> HttpStatus.CONFLICT;      // Email already exists
                  // Student code already exists
            
            // 400 Bad Request - Validation errors
            case "400" -> HttpStatus.BAD_REQUEST;   // Invalid email format
                // Student code invalid
            
            // 404 Not Found
            case "404" -> HttpStatus.NOT_FOUND;
            
            // 500 Internal Server Error - Default for unknown errors
            case "500" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

