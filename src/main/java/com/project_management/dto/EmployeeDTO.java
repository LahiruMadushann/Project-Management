package com.project_management.dto;

import com.project_management.models.enums.Domain;
import com.project_management.models.enums.RoleCategory;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeDTO {
    private String employeeId;
    private String employeeName;
    private String seniority;
    private String roleName;
    private RoleCategory roleCategory;
    private String createdByUsername;
    private List<EmployeeSkillDTO> skills;
    private List<EmployeeExperienceDTO> experiences;
    private List<EmployeeEducationDTO> educations;
    private Long userId;
    private Integer maximumAssessedCount;
    private Integer difficultyLevel;
    private Domain domain;
}
