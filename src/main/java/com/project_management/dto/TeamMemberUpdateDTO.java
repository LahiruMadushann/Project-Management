package com.project_management.dto;

import lombok.Data;

@Data
public class TeamMemberUpdateDTO {
    private String currentEmployeeId;  // Current employee in the role (can be null for new assignments)
    private String newEmployeeId;      // New employee to assign
    private String roleName;
}