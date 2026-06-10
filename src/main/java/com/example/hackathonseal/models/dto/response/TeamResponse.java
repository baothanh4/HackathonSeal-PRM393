package com.example.hackathonseal.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private Long teamId;
    private String name;
    private Long eventId;
    private Long leaderId;
    private String leaderName;
    private Long categoryId;
    private String categoryName;
    private long memberCount;
    private List<TeamMemberResponse> members;
}
