package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.AccountStatus;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.ParticipantType;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.ForgotPasswordRequest;
import com.example.hackathonseal.models.dto.request.LoginRequest;
import com.example.hackathonseal.models.dto.request.RegisterRequest;
import com.example.hackathonseal.models.dto.request.ResetPasswordRequest;
import com.example.hackathonseal.models.dto.request.VerifyEmailRequest;
import com.example.hackathonseal.models.dto.response.AuthResponse;
import com.example.hackathonseal.models.dto.response.RegisterResponse;
import com.example.hackathonseal.models.entity.EmailVerificationToken;
import com.example.hackathonseal.models.entity.PasswordResetToken;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.models.entity.UserProfile;
import com.example.hackathonseal.repo.EmailVerificationTokenRepository;
import com.example.hackathonseal.repo.PasswordResetTokenRepository;
import com.example.hackathonseal.repo.UserProfileRepository;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ValidationService validationService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.email-verification-token-expiry-minutes:1440}")
    private int emailVerificationOtpExpiryMinutes;

    @Value("${app.password-reset-token-expiry-minutes:15}")
    private int passwordResetOtpExpiryMinutes;

    // ==================== LOGIN ====================

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL_OR_PASSWORD));

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new AppException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        if (user.getStatus() != AccountStatus.APPROVED) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_APPROVED);
        }

        // Block login if email not verified
        if (Boolean.FALSE.equals(user.getIsEmailVerified())) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .id(user.getId())
                .build();
    }

    // ==================== REGISTER ====================

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate input
        validationService.validateRegister(request);

        // Create user (email NOT verified yet)
        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(UserRole.STUDENT)
                .status(AccountStatus.APPROVED)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Save user profile
        if (request.getParticipantType() == ParticipantType.EXTERNAL_STUDENT) {
            UserProfile userProfile = UserProfile.builder()
                    .user(user)
                    .participantType(ParticipantType.EXTERNAL_STUDENT)
                    .studentCode(request.getStudentCode().trim())
                    .universityName(request.getUniversityName().trim())
                    .build();
            userProfileRepository.save(userProfile);
        } else {
            UserProfile userProfile = UserProfile.builder()
                    .user(user)
                    .participantType(ParticipantType.FPT_STUDENT)
                    .universityName("FPT University")
                    .studentCode(request.getStudentCode().trim())
                    .build();
            userProfileRepository.save(userProfile);
        }

        // Generate OTP and send verification email
        String otp = generateAndSaveEmailVerificationOtp(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), otp);

        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Registration successful. A 6-digit OTP has been sent to your email.")
                .status("PENDING_EMAIL_VERIFICATION")
                .build();
    }

    // ==================== VERIFY EMAIL ====================

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByUserEmailAndOtp(email, otp)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if (verificationToken.getUsed()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        if (verificationToken.isExpired()) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        // Mark OTP as used
        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        // Mark user email as verified
        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    // ==================== RESEND VERIFICATION OTP ====================

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // Delete old OTPs
        emailVerificationTokenRepository.deleteAllByUser(user);

        // Generate and send new OTP
        String otp = generateAndSaveEmailVerificationOtp(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), otp);

        log.info("Verification OTP resent to: {}", user.getEmail());
    }

    // ==================== FORGOT PASSWORD ====================

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Always behave the same way (security: don't reveal if email exists)
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete old OTPs
            passwordResetTokenRepository.deleteAllByUser(user);

            // Generate and send new OTP
            String otp = generateAndSavePasswordResetOtp(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), otp);

            log.info("Password reset OTP sent to: {}", user.getEmail());
        });
    }

    // ==================== RESET PASSWORD ====================

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByUserEmailAndOtp(email, otp)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));

        if (resetToken.getUsed()) {
            throw new AppException(ErrorCode.RESET_TOKEN_ALREADY_USED);
        }

        if (resetToken.isExpired()) {
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        }

        // Validate new password strength
        validationService.validatePassword(request.getNewPassword());

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Generate a cryptographically secure 6-digit OTP (100000–999999)
     */
    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    private String generateAndSaveEmailVerificationOtp(User user) {
        String otp = generateOtp();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(otp)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(emailVerificationOtpExpiryMinutes))
                .build();
        emailVerificationTokenRepository.save(token);
        return otp;
    }

    private String generateAndSavePasswordResetOtp(User user) {
        String otp = generateOtp();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(passwordResetOtpExpiryMinutes))
                .build();
        passwordResetTokenRepository.save(token);
        return otp;
    }
}
