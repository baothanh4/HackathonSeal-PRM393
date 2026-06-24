package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.UpdateStudentProfileRequest;
import com.example.hackathonseal.models.dto.response.StudentProfileResponse;
import com.example.hackathonseal.models.entity.User;

public interface StudentProfileService {
    StudentProfileResponse getStudentProfile(User currentUser);
    StudentProfileResponse updateStudentProfile(User currentUser, UpdateStudentProfileRequest request);
}
