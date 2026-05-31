package com.example.hackthonseal.models.dto.response;

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
public class TeamMemberResponse {
    private Long registrationId;
    private Long userId; // null if guest
    private String email;
    private String fullName;
    private String studentCode;
    private String university;
    private boolean guest;
}
