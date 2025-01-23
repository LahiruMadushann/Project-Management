package com.project_management.repositories;

import com.project_management.models.ProjectBudget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectBudgetRepository extends JpaRepository<ProjectBudget,Long> {
}
