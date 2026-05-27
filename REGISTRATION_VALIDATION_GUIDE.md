# Registration & Validation System Documentation

## Overview
This document describes the comprehensive validation, error handling, and registration system for the HackthonSeal application.

## Core Components

### 1. Error Code Enum (`ErrorCode.java`)
Centralized error code enumeration for consistent error handling across the application.

**Example Error Codes:**
- `AUTH_001`: Invalid email or password
- `AUTH_003`: Email already exists
- `AUTH_005`: Password must be at least 8 characters
- `EXT_001`: University name is required for external students
- `EXT_005`: Student code already exists

### 2. Exception Class (`AppException.java`)
Custom exception class that wraps error codes with messages.

```java
// Usage
throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
throw new AppException(ErrorCode.PASSWORD_WEAK, "Custom message");
```

### 3. Global Exception Handler (`GlobalExceptionHandler.java`)
Centralized exception handling for all endpoints with consistent error responses.

**Features:**
- Handles `AppException` with appropriate HTTP status codes
- Handles validation exceptions with detailed field error messages
- Returns `ErrorResponse` with error code, message, and timestamp

### 4. Validation Service (`ValidationService.java`)
Service layer for comprehensive input validation.

**Validations Performed:**
- ✅ Full Name: 2-100 characters
- ✅ Email: Valid format and unique (not in database)
- ✅ Password: 
  - Minimum 8 characters
  - Contains uppercase letter
  - Contains lowercase letter
  - Contains digit
  - Contains special character (@$!%*?&)
- ✅ Participant Type: Required
- ✅ University Name: 2-100 characters (for external students)
- ✅ Student Code: 3-20 characters, unique in database (for external students)

### 5. Custom Validation Annotations
- `@ValidPassword`: Validates password strength
- `@ValidFullName`: Validates full name format

## Registration Flow

### Request Body Example

#### FPT Student Registration
```json
{
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@student.fpt.edu.vn",
  "password": "SecurePass123!",
  "participantType": "FPT_STUDENT"
}
```

#### External Student Registration
```json
{
  "fullName": "Trần Thị B",
  "email": "tranthib@email.com",
  "password": "SecurePass456!",
  "participantType": "EXTERNAL_STUDENT",
  "universityName": "University of Technology",
  "studentCode": "UT2024001"
}
```

### Registration Endpoint
- **URL:** `POST /api/v1/auth/register`
- **Status Code:** 201 Created
- **Response:** `RegisterResponse`

### Response Example
```json
{
  "id": 1,
  "email": "nguyenvana@student.fpt.edu.vn",
  "fullName": "Nguyễn Văn A",
  "message": "Registration successful. Please verify your email.",
  "status": "PENDING"
}
```

## Error Responses

### Email Already Exists
```json
{
  "errorCode": "AUTH_003",
  "message": "Email already exists",
  "timestamp": "2026-05-27T10:30:00"
}
```

### Invalid Password
```json
{
  "errorCode": "AUTH_006",
  "message": "Password must contain uppercase, lowercase, numbers and special characters",
  "timestamp": "2026-05-27T10:30:00"
}
```

### Missing University (External Student)
```json
{
  "errorCode": "EXT_001",
  "message": "University name is required for external students",
  "timestamp": "2026-05-27T10:30:00"
}
```

## Database Schema

### User Entity
- `id`: Long (Primary Key)
- `email`: String (Unique, Not Null)
- `password`: String (Not Null)
- `fullName`: String (Not Null)
- `role`: UserRole (STUDENT, MENTOR, JUDGE, COORDINATOR, ADMIN)
- `status`: AccountStatus (PENDING, APPROVED, REJECTED, SUSPENDED)
- `isEmailVerified`: Boolean
- `createdAt`: LocalDateTime

### UserProfile Entity
- `id`: Long (Primary Key)
- `user_id`: Long (Foreign Key, One-to-One)
- `participantType`: ParticipantType (FPT_STUDENT, EXTERNAL_STUDENT)
- `studentCode`: String (Unique, for external students)
- `universityName`: String (for external students)

## ParticipantType Enum
```java
public enum ParticipantType {
    FPT_STUDENT,         // FPT học viên
    EXTERNAL_STUDENT     // Sinh viên bên ngoài
}
```

## Password Requirements
- **Minimum Length:** 8 characters
- **Must Include:**
  - At least one uppercase letter (A-Z)
  - At least one lowercase letter (a-z)
  - At least one digit (0-9)
  - At least one special character (@, $, !, %, *, ?, &)

**Example Valid Passwords:**
- `SecurePass123!`
- `MyPassword@2024`
- `Test$Pw123`

**Example Invalid Passwords:**
- `password` (no uppercase, no digit, no special char)
- `Password123` (no special character)
- `Pass@1` (too short)
- `PASSWORD@123` (no lowercase)

## Transaction Support
The `register()` method is marked with `@Transactional` to ensure:
- All database operations are atomic
- If any step fails, the entire transaction is rolled back
- Prevents orphaned User records without UserProfile

## Security Best Practices Implemented
✅ Passwords are encoded using PasswordEncoder (BCrypt)
✅ Email is normalized (trimmed and lowercased)
✅ Student code uniqueness validation
✅ Input validation before database operations
✅ Comprehensive error messages without exposing sensitive information
✅ Account status management (PENDING vs APPROVED)

## Future Enhancements
- Email verification token generation and validation
- Password reset functionality
- Account confirmation workflow
- Rate limiting for register endpoint
- Audit logging for registration attempts

