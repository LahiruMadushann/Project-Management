package com.project_management.dto;

import lombok.Data;

@Data
public class EmployeeDTO {
    private String employeeId;
    private String employeeName;
    private String seniority;
    private String createdByUsername;
}
