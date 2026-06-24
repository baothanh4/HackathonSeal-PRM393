package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByStudentCode(String studentCode);

    boolean existsByStudentCode(String studentCode);
}

