package com.example.hackathonseal.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankingResponse {
    private Long teamId;
    private String teamName;
    private Long categoryId;
    private String categoryName;
    private Double averageScore;
    private Integer rank;
    private Boolean isEligibleNextRound;
}
