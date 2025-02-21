package com.project_management.dto;

import lombok.Data;

@Data
public class CategoryRole {
    private String roleCategory;
    private String roleName;
    private double roleDistributionValue;

    public static CategoryRole fromEmployeeDTO(PerfectEmployeeDTO dto) {
        CategoryRole categoryRole = new CategoryRole();
        categoryRole.setRoleCategory(String.valueOf(dto.getRoleCategory()));
        categoryRole.setRoleName(dto.getRoleName());
        categoryRole.setRoleDistributionValue(dto.getRoleDistributionValue());
        return categoryRole;
    }
}

