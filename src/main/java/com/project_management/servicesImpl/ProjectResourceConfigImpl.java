package com.project_management.servicesImpl;

import com.project_management.dto.ProjectDTO;
import com.project_management.models.Project;
import com.project_management.models.ProjectResourceConfig;
import com.project_management.repositories.ProjectResourceConfigRepository;
import com.project_management.services.ProjectResourceConfigService;
import com.project_management.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProjectResourceConfigImpl  implements ProjectResourceConfigService {

    @Autowired
    private ProjectResourceConfigRepository projectResourceConfigRepository;

    @Autowired
    private ProjectService projectService;

    @Override
    public ProjectResourceConfig saveConfig(ProjectResourceConfig config) {
        return projectResourceConfigRepository.save(config);
    }

    @Override
    public ProjectResourceConfig getConfigByProject(long id) {
        ProjectResourceConfig projectResourceConfig = projectResourceConfigRepository.findTopByProjectIdOrderByIdDesc(id);
        ProjectDTO project = projectService.getProjectById(id);
        // Round to the nearest 10,000
        BigDecimal budget = project.getPredictedBudgetForResources();
        BigDecimal roundedBudget = budget
                .divide(new BigDecimal("10000"), 0, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("10000"));

        projectResourceConfig.setResourceBudget(roundedBudget);

        return projectResourceConfigRepository.findTopByProjectIdOrderByIdDesc(id);
    }
}
