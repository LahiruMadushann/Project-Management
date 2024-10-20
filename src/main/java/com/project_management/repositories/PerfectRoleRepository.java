package com.project_management.repositories;

import com.project_management.models.PerfectRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfectRoleRepository extends JpaRepository<PerfectRole, Long> {
    boolean existsByRoleName(String roleName);
}
