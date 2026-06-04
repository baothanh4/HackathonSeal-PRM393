package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.dto.request.RuleRequest;
import com.example.hackathonseal.models.dto.response.RuleResponse;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.Rule;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.RuleRepository;
import com.example.hackathonseal.services.Interface.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleServiceImpl implements RuleService {

    private final RuleRepository ruleRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public RuleResponse createRule(Long eventId, RuleRequest request) {
        log.info("Creating rule for event ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Rule rule = Rule.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .event(event)
                .build();

        rule = ruleRepository.save(rule);
        log.info("Rule created successfully with ID: {}", rule.getId());
        return mapToResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getRulesByEventId(Long eventId) {
        log.info("Fetching rules for event ID: {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return ruleRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RuleResponse getRuleById(Long id) {
        log.info("Fetching rule ID: {}", id);
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RULE_NOT_FOUND));
        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public RuleResponse updateRule(Long id, RuleRequest request) {
        log.info("Updating rule ID: {}", id);
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RULE_NOT_FOUND));

        rule.setName(request.getName().trim());
        rule.setDescription(request.getDescription().trim());

        rule = ruleRepository.save(rule);
        log.info("Rule ID: {} updated successfully", id);
        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting rule ID: {}", id);
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RULE_NOT_FOUND));

        ruleRepository.delete(rule);
        log.info("Rule ID: {} deleted successfully", id);
    }

    private RuleResponse mapToResponse(Rule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .eventId(rule.getEvent().getId())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
