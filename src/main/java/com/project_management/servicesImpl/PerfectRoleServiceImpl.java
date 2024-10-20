package com.project_management.servicesImpl;

import com.project_management.models.PerfectRole;
import com.project_management.repositories.PerfectRoleRepository;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfectRoleServiceImpl implements PerfectRoleService {

    @Autowired
    private PerfectRoleRepository perfectRoleRepository;

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
}
