package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.CategoryRequest;
import com.example.hackathonseal.models.dto.response.CategoryResponse;
import com.example.hackathonseal.models.entity.Category;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.CategoryRepository;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.services.Interface.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(Long eventId, CategoryRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .event(event)
                .build();

        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<Category> categories = categoryRepository.findByEvent(event);
        return categories.stream().map(this::mapToCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse assignMentorToCategory(Long eventId, Long categoryId, Long mentorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        if (!category.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category does not belong to this event");
        }

        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Mentor user not found"));

        if (mentor.getRole() != UserRole.MENTOR) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "User is not a mentor");
        }

        if (!category.getMentors().contains(mentor)) {
            category.getMentors().add(mentor);
            category = categoryRepository.save(category);
        }

        return mapToCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse assignJudgeToCategory(Long eventId, Long categoryId, Long judgeId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));

        if (!category.getEvent().getId().equals(event.getId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Category does not belong to this event");
        }

        User judge = userRepository.findById(judgeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Judge user not found"));

        if (judge.getRole() != UserRole.JUDGE) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "User is not a judge");
        }

        if (!category.getJudges().contains(judge)) {
            category.getJudges().add(judge);
            category = categoryRepository.save(category);
        }

        return mapToCategoryResponse(category);
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        List<CategoryResponse.UserSummary> mentors = category.getMentors().stream()
                .map(u -> CategoryResponse.UserSummary.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .build())
                .toList();

        List<CategoryResponse.UserSummary> judges = category.getJudges().stream()
                .map(u -> CategoryResponse.UserSummary.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .build())
                .toList();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .eventId(category.getEvent().getId())
                .mentors(mentors)
                .judges(judges)
                .build();
    }
}
