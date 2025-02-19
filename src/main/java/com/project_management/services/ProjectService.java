package com.project_management.services;

import com.project_management.dto.ProjectDTO;
import com.project_management.dto.ProjectDetailsDTO;
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
}
