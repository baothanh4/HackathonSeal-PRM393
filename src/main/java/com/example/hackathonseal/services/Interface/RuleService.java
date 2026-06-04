package com.example.hackathonseal.services.Interface;

import com.example.hackathonseal.models.dto.request.RuleRequest;
import com.example.hackathonseal.models.dto.response.RuleResponse;

import java.util.List;

public interface RuleService {
    RuleResponse createRule(Long eventId, RuleRequest request);

    List<RuleResponse> getRulesByEventId(Long eventId);

    RuleResponse getRuleById(Long id);

    RuleResponse updateRule(Long id, RuleRequest request);

    void deleteRule(Long id);
}
