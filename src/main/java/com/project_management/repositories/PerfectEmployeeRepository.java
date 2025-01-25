package com.project_management.repositories;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.enums.RoleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerfectEmployeeRepository extends JpaRepository<PerfectEmployee, String> {
    List<PerfectEmployee> findByRoleCategory(RoleCategory roleCategory);
}
