package com.project_management.dto;

import com.project_management.models.Employee;
import lombok.Data;

@Data
public class PerfectEmployeeExperienceDTO {
    private Long employeeExperienceId;

    private Employee employee;

    private String experienceName;

    private Integer experiencePoints;

    private Integer experienceWeight;
}
