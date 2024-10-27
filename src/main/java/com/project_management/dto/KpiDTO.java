package com.project_management.dto;

import lombok.Data;

@Data
public class KpiDTO {
    private String employeeId;
    private String employeeName;
    private String roleName;
    private double overallKpi;
    private double skillKpi;
    private double educationKpi;
    private double experienceKpi;
}