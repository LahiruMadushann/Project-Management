package com.project_management.dto;

import com.project_management.models.Employee;
import lombok.Data;

@Data
public class PerfectEmployeeEducationDTO {
    private Long employeeEducationId;

    private Employee employee;

    private String educationName;

    private Integer educationPoints;

    private Integer educationWeight;
}
