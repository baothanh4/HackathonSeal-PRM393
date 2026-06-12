package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.CriteriaTemplateRequest;
import com.example.hackathonseal.models.dto.response.CriteriaTemplateResponse;
import com.example.hackathonseal.models.entity.CriteriaTemplate;
import com.example.hackathonseal.repo.CriteriaTemplateRepository;
import com.example.hackathonseal.services.Interface.CriteriaTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CriteriaTemplateServiceImpl implements CriteriaTemplateService {

    private final CriteriaTemplateRepository criteriaTemplateRepository;

    @Override
    @Transactional
    public CriteriaTemplateResponse createTemplate(CriteriaTemplateRequest request) {
        CriteriaTemplate template = CriteriaTemplate.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .defaultMaxScore(request.getDefaultMaxScore())
                .defaultWeight(request.getDefaultWeight())
                .build();
        template = criteriaTemplateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaTemplateResponse> getAllTemplates() {
        return criteriaTemplateRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CriteriaTemplateResponse getTemplateById(Long id) {
        CriteriaTemplate template = criteriaTemplateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria template not found"));
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public CriteriaTemplateResponse updateTemplate(Long id, CriteriaTemplateRequest request) {
        CriteriaTemplate template = criteriaTemplateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria template not found"));

        template.setName(request.getName().trim());
        template.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        template.setDefaultMaxScore(request.getDefaultMaxScore());
        template.setDefaultWeight(request.getDefaultWeight());

        template = criteriaTemplateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        CriteriaTemplate template = criteriaTemplateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Criteria template not found"));
        criteriaTemplateRepository.delete(template);
    }

    private CriteriaTemplateResponse mapToResponse(CriteriaTemplate template) {
        return CriteriaTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .defaultMaxScore(template.getDefaultMaxScore())
                .defaultWeight(template.getDefaultWeight())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
