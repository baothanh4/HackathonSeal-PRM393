package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.PasswordResetToken;
import com.example.hackathonseal.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find a reset OTP by the user's email and OTP code (not used yet)
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.email = :email AND t.token = :otp AND t.used = false")
    Optional<PasswordResetToken> findByUserEmailAndOtp(String email, String otp);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
    void deleteAllByUser(User user);
}
