package com.project_management.servicesImpl;

import com.project_management.models.ProjectResourceConfig;
import com.project_management.repositories.ProjectResourceConfigRepository;
import com.project_management.services.ProjectResourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectResourceConfigImpl  implements ProjectResourceConfigService {

    @Autowired
    private ProjectResourceConfigRepository projectResourceConfigRepository;

    @Override
    public ProjectResourceConfig saveConfig(ProjectResourceConfig config) {
        return projectResourceConfigRepository.save(config);
    }

    @Override
    public ProjectResourceConfig getConfigByProject(long id) {
        return projectResourceConfigRepository.findByProjectId(id);
    }
}
