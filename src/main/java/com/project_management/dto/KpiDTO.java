package com.project_management.dto;

import com.project_management.models.enums.Domain;
import com.project_management.models.enums.RoleCategory;
import lombok.Data;

@Data
public class KpiDTO {
    private String employeeId;
    private String employeeName;
    private String roleName;
    private RoleCategory roleCategory;
    private double overallKpi;
    private double skillKpi;
    private double educationKpi;
    private double experienceKpi;
    private Domain domain;
}