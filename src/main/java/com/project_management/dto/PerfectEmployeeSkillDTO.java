package com.project_management.dto;

import com.project_management.models.Employee;
import lombok.Data;

@Data
public class PerfectEmployeeSkillDTO {
    private Long employeeSkillId;

    private Employee employee;

    private String skillName;

    private Integer skillPoints;

    private Integer skillWeight;
}
