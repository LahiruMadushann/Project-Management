package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamUpdateDTO {
    private Long projectId;
    private List<TeamMemberUpdateDTO> teamMembers;
}