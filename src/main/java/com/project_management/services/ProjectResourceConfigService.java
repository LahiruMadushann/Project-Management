package com.project_management.services;

import com.project_management.models.Project;
import com.project_management.models.ProjectResourceConfig;

import java.util.List;

public interface ProjectResourceConfigService {
    ProjectResourceConfig saveConfig(ProjectResourceConfig config);
    List<ProjectResourceConfig > getConfigByProject(long id);
}
