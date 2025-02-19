package com.project_management.servicesImpl;

import com.project_management.dto.ProjectDTO;
import com.project_management.dto.ProjectDetailsDTO;
import com.project_management.models.Project;
import com.project_management.models.enums.ProjectStatus;
import com.project_management.repositories.ProjectRepository;
import com.project_management.services.ProjectService;
import com.project_management.services.ReleaseVersionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = new Project();
        projectDTO.setStatus(ProjectStatus.PENDING);
        BeanUtils.copyProperties(projectDTO, project);

        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return convertToDTO(project);
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjectsBudgetActive() {
        return projectRepository.findAll().stream()
                .filter(project -> project.getPredictedBudget() != null && project.getPredictedBudget().compareTo(BigDecimal.ZERO) > 0)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (projectDTO.getName() != null) {
            existingProject.setName(projectDTO.getName());
        }
        if (projectDTO.getSummary() != null) {
            existingProject.setSummary(projectDTO.getSummary());
        }
        if (projectDTO.getDomain() != null) {
            existingProject.setDomain(projectDTO.getDomain());
        }
        if (projectDTO.getBudget() != null) {
            existingProject.setBudget(projectDTO.getBudget());
        }
        if (projectDTO.getDeadline() != null) {
            existingProject.setDeadline(projectDTO.getDeadline());
        }

        existingProject.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(existingProject);
        return convertToDTO(updatedProject);
    }

    @Override
    public ProjectDTO updateProjectStatus(Long projectId, String status) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (status != null) {
            existingProject.setStatus(ProjectStatus.valueOf(status));
        }

        existingProject.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(existingProject);
        return convertToDTO(updatedProject);
    }

    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDetailsDTO> getProjectsAssignedToUser(Long userId) {
        return projectRepository.findByTasksAssignedUserId(userId).stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        return projectDTO;
    }

    private ProjectDetailsDTO convertToProjectDTO(Project project) {
        ProjectDetailsDTO dto = new ProjectDetailsDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setSummary(project.getSummary());
        dto.setDomain(project.getDomain());
        dto.setBudget(project.getBudget());
        dto.setDeadline(project.getDeadline());
        dto.setStatus(project.getStatus());
        dto.setPredictedBudget(project.getPredictedBudget());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        dto.setReleaseVersions(releaseVersionService.getReleaseVersionsByProjectIdNew(project.getId()));
        return dto;
    }
}
