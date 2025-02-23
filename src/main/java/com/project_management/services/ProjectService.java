package com.project_management.services;

import com.project_management.dto.*;
import com.project_management.models.AdvanceDetails;
import com.project_management.models.ProjectBudget;
import com.project_management.models.enums.ProjectStatus;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO);
    ProjectDTO getProjectById(Long id);
    List<ProjectDTO> getAllProjects();
    public List<ProjectDTO> getAllProjectsBudgetActive();
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    ProjectDTO updateProjectStatus(Long projectId, String status);
    void deleteProject(Long id);
    List<ProjectDetailsDTO> getProjectsAssignedToUser(Long userId);
    EffortCombinedCallResponse saveAdvance(AdvanceDetailsDTO advanceDetailsDTO);
    AdvanceDetails getAdvanceDetailsByProjectId(Long projectId);
    ProjectBudget saveBudget(ProjectBudgetGraphDto dto);
    AdvanceDetails getAdvanceDetailsById(Long id);
}
