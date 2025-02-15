package com.project_management.services;

import com.project_management.models.ProjectResourceConfig;

public interface ProjectBudgetService {
    ProjectBudgetService saveBudget(ProjectBudgetService budget);
    ProjectResourceConfig getBudgetByProjectId(Long id);
}
