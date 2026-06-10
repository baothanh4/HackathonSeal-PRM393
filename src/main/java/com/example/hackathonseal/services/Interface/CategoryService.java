package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.CategoryRequest;
import com.example.hackathonseal.models.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(Long eventId, CategoryRequest request);
    List<CategoryResponse> getCategoriesByEvent(Long eventId);
    CategoryResponse assignMentorToCategory(Long eventId, Long categoryId, Long mentorId);
    CategoryResponse assignJudgeToCategory(Long eventId, Long categoryId, Long judgeId);
}
