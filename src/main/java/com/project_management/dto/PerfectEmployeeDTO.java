package com.project_management.dto;

import com.project_management.models.PerfectEmployeeEducation;
import com.project_management.models.PerfectEmployeeExperience;
import com.project_management.models.PerfectEmployeeSkill;
import com.project_management.models.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class PerfectEmployeeDTO {
    private String employeeId;
    private String roleName;
    private Long createdByUserId;
    private List<PerfectEmployeeSkillDTO> skills;
    private List<PerfectEmployeeExperienceDTO> experiences;
    private List<PerfectEmployeeEducationDTO> educations;
}
