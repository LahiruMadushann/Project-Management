package com.project_management.services;

import com.project_management.models.PerfectRole;

import java.util.List;

public interface PerfectRoleService {
    PerfectRole saveRole(String roleName);
    List<PerfectRole> getAllRoles();
}

