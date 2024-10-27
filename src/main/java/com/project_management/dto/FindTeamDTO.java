package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class FindTeamDTO {
    private Long projectId;
    private List<TeamRolesDTO> requiredRoles;
    private  List<TeamSkillsDTO> requiredSkills;
}
