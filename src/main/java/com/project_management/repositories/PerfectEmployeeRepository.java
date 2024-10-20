package com.project_management.repositories;

import com.project_management.models.PerfectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfectEmployeeRepository extends JpaRepository<PerfectEmployee, String> {
}
