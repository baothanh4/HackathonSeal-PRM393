package com.example.hackathonseal.controller;

import com.example.hackathonseal.models.dto.request.ForgotPasswordRequest;
import com.example.hackathonseal.models.dto.request.LoginRequest;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.models.dto.request.ResetPasswordRequest;
import com.example.hackathonseal.models.dto.request.VerifyEmailRequest;
import com.example.hackathonseal.models.dto.response.AuthResponse;
import com.example.hackathonseal.models.dto.response.RegisterResponse;
import com.example.hackathonseal.services.Interface.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, OTP email verification and password reset")
public class AuthController {

    private final AuthService authService;

    // ==================== LOGIN ====================

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ==================== REGISTER ====================

    @PostMapping("/register")
    @Operation(summary = "Register a new account — sends 6-digit OTP to email for verification")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== EMAIL VERIFICATION VIA OTP ====================

    @PostMapping("/verify-email")
    @Operation(
        summary = "Verify email address using 6-digit OTP",
        description = "Body: { \"email\": \"user@example.com\", \"otp\": \"123456\" }"
    )
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully. You can now log in.",
                "status", "SUCCESS"
        ));
    }

    @PostMapping("/resend-verification")
    @Operation(
        summary = "Resend email verification OTP",
        description = "Send a new 6-digit OTP to the registered email"
    )
    public ResponseEntity<Map<String, String>> resendVerification(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of(
                "message", "A new OTP has been sent to your email.",
                "status", "SUCCESS"
        ));
    }

    // ==================== FORGOT / RESET PASSWORD VIA OTP ====================

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Request password reset — sends 6-digit OTP to email",
        description = "Body: { \"email\": \"user@example.com\" }"
    )
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        // Always return success (security: don't reveal if email exists)
        return ResponseEntity.ok(Map.of(
                "message", "If your email is registered, a 6-digit OTP has been sent.",
                "status", "SUCCESS"
        ));
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password using email + 6-digit OTP",
        description = "Body: { \"email\": \"user@example.com\", \"otp\": \"123456\", \"newPassword\": \"NewPass@123\" }"
    )
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully. You can now log in.",
                "status", "SUCCESS"
        ));
    }
}
