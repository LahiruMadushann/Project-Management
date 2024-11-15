package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeDTO {
    private String employeeId;
    private String employeeName;
    private String seniority;
    private String roleName;
    private String createdByUsername;
    private List<EmployeeSkillDTO> skills;
    private List<EmployeeExperienceDTO> experiences;
    private List<EmployeeEducationDTO> educations;
    private Long userId;
    private Integer maximumAssessedCount;
    private Integer difficultyLevel;
}
