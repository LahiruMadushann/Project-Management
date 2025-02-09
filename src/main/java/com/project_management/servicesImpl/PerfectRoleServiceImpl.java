package com.project_management.servicesImpl;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.PerfectRole;
import com.project_management.models.enums.RoleCategory;
import com.project_management.repositories.PerfectEmployeeRepository;
import com.project_management.repositories.PerfectRoleRepository;
import com.project_management.repositories.ProjectRepository;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfectRoleServiceImpl implements PerfectRoleService {

    @Autowired
    private PerfectRoleRepository perfectRoleRepository;

    @Autowired
    private PerfectEmployeeRepository perfectEmployeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public PerfectRole saveRole(String roleName) {
        if (!perfectRoleRepository.existsByRoleName(roleName)) {
            PerfectRole role = new PerfectRole();
            role.setRoleName(roleName);
            return perfectRoleRepository.save(role);
        }
        return null;
    }

    @Override
    public List<PerfectRole> getAllRoles() {
        return perfectRoleRepository.findAll();
    }

    @Override
    public List<PerfectEmployee> getAllRolesCategory(RoleCategory roleCategory) {
        return perfectEmployeeRepository.findByRoleCategory(roleCategory);
    }

    @Override
    public String getAllRolesById(String employeeId) {
        return perfectEmployeeRepository.findRolesByEmployeeId(employeeId);
    }
}
