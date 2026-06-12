package com.example.hackathonseal.models.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeAssignmentResponse {
    private Long id;
    private Long judgeId;
    private String judgeName;
    private String judgeEmail;
    private Long roundId;
    private String roundName;
    private Long categoryId;
    private String categoryName;
    private String judgeType;
    private LocalDateTime assignedAt;
}
