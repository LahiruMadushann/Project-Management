package com.project_management.services;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.PerfectRole;
import com.project_management.models.enums.RoleCategory;

import java.util.List;

public interface PerfectRoleService {
    PerfectRole saveRole(String roleName);
    List<PerfectRole> getAllRoles();
    List<PerfectEmployee>  getAllRolesCategory(RoleCategory roleCategory);
}

