package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.AdvanceDetails;
import com.project_management.models.Project;
import com.project_management.models.enums.ProjectStatus;
import com.project_management.repositories.AdvanceDetailsRepository;
import com.project_management.repositories.ProjectRepository;
import com.project_management.services.ProjectService;
import com.project_management.services.ReleaseVersionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private AdvanceDetailsRepository advanceDetailsRepository;

    @Autowired
    RestTemplate restTemplate;

    @Value("${ml.service.url.effort}")
    private String effortURL;

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

    @Override
    public AdvanceDetails saveAdvance(AdvanceDetailsDTO advanceDetailsDTO) {

        EffortRequestDto effortRequestDto = new EffortRequestDto();
        effortRequestDto.setMethodology(advanceDetailsDTO.getMethodology());
        effortRequestDto.setScheduleQuality(advanceDetailsDTO.getScheduleQuality());
        effortRequestDto.setMultiLang(1);
        effortRequestDto.setProgrammingLang(advanceDetailsDTO.getProgrammingLang());
        effortRequestDto.setDbms(advanceDetailsDTO.getDbms());
        effortRequestDto.setStandards(advanceDetailsDTO.getStandard());
        effortRequestDto.setAccuracy(advanceDetailsDTO.getRequirementAccuracy());
        effortRequestDto.setDocumentation(advanceDetailsDTO.getDocumentation());
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EffortRequestDto> entity = new HttpEntity<>(effortRequestDto, headers);

        // Send the request to the ML service
        ResponseEntity<EffortResponseDto> mlResponse = restTemplate.exchange(
                effortURL,
                HttpMethod.POST,
                entity,
                EffortResponseDto.class
        );

        // Check the response status and handle errors
        if (!mlResponse.getStatusCode().is2xxSuccessful() || mlResponse.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " +
                    mlResponse.getStatusCode());
        }
        System.out.println(mlResponse.getBody().getEffort());
        return advanceDetailsRepository.save(convertToAdvanceModel(advanceDetailsDTO));
    }

    @Override
    public AdvanceDetails getAdvanceDetailsByProjectId(Long projectId) {
        return advanceDetailsRepository.findByProjectId(projectId);
    }

    @Override
    public AdvanceDetails getAdvanceDetailsById(Long id) {
        return advanceDetailsRepository.findById(id).orElseThrow();
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        return projectDTO;
    }

    private AdvanceDetails convertToAdvanceModel(AdvanceDetailsDTO dto) {
        AdvanceDetails entity = new AdvanceDetails();
        entity.setProjectId(dto.getProjectId());
        entity.setDomain(dto.getDomain());
        entity.setMethodology(dto.getMethodology());
        entity.setProgrammingLang(dto.getProgrammingLang());
        entity.setDbms(dto.getDbms());
        entity.setDevops(dto.getDevops());
        entity.setIntegration(dto.getIntegration());
        entity.setMl(dto.getMl());
        entity.setSecurityLevel(dto.getSecurityLevel());
        entity.setUserCount(dto.getUserCount());
        entity.setDuration(dto.getDuration());
        entity.setScheduleQuality(dto.getScheduleQuality());
        entity.setStandard(dto.getStandard());
        entity.setRequirementAccuracy(dto.getRequirementAccuracy());
        entity.setDocumentation(dto.getDocumentation());
        entity.setExpectedProfit(dto.getExpectedProfit());
        entity.setOtherExpenses(dto.getOtherExpenses());
        return entity;
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
