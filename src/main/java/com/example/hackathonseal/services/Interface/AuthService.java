package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.ForgotPasswordRequest;
import com.example.hackathonseal.models.dto.request.LoginRequest;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.models.dto.request.ResetPasswordRequest;
import com.example.hackathonseal.models.dto.request.VerifyEmailRequest;
import com.example.hackathonseal.models.dto.response.AuthResponse;
import com.example.hackathonseal.models.dto.response.RegisterResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    /** Verify email using 6-digit OTP sent to the user's email */
    void verifyEmail(VerifyEmailRequest request);

    /** Resend a new OTP to the user's email */
    void resendVerificationEmail(String email);

    /** Send a 6-digit OTP to the user's email for password reset */
    void forgotPassword(ForgotPasswordRequest request);

    /** Reset password using email + 6-digit OTP + new password */
    void resetPassword(ResetPasswordRequest request);
}
