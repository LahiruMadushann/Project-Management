package com.project_management.services;

import com.project_management.models.Project;
import com.project_management.models.ProjectResourceConfig;

public interface ProjectResourceConfigService {
    ProjectResourceConfig saveConfig(ProjectResourceConfig config);
    ProjectResourceConfig getConfigByProject(long id);
}
