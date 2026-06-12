package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.CriteriaTemplateRequest;
import com.example.hackathonseal.models.dto.response.CriteriaTemplateResponse;

import java.util.List;

public interface CriteriaTemplateService {
    CriteriaTemplateResponse createTemplate(CriteriaTemplateRequest request);
    List<CriteriaTemplateResponse> getAllTemplates();
    CriteriaTemplateResponse getTemplateById(Long id);
    CriteriaTemplateResponse updateTemplate(Long id, CriteriaTemplateRequest request);
    void deleteTemplate(Long id);
}
