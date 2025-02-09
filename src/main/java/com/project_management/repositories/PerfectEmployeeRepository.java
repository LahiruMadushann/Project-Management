package com.project_management.repositories;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.enums.RoleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerfectEmployeeRepository extends JpaRepository<PerfectEmployee, String> {
    List<PerfectEmployee> findByRoleCategory(RoleCategory roleCategory);

    @Query("SELECT pe.roleName FROM PerfectEmployee pe WHERE pe.employeeId = :employeeId")
    String findRolesByEmployeeId(@Param("employeeId") String employeeId);
}
