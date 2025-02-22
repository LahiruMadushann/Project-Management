package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.enums.ProjectStatus;
import com.project_management.models.enums.RoleCategory;
import com.project_management.repositories.AdvanceDetailsRepository;
import com.project_management.repositories.CalculationResultRepository;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ProjectResourceConfigRepository;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.PerfectEmployeeService;
import com.project_management.services.ProjectService;
import com.project_management.services.ReleaseVersionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @Autowired
    private PerfectEmployeeService perfectEmployeeService;

    @Autowired
    private AdvanceDetailsRepository advanceDetailsRepository;

    @Autowired
    private ProjectResourceConfigRepository projectResourceConfigRepository;

    @Autowired
    private CalculationResultRepository calculationResultRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @Value("${ml.service.url.effort}")
    private String effortURL;

    @Value("${ml.service.url.resources}")
    private String resourceMLURL;

    @Value("${ml.service.url.calculateHeads}")
    private String calculateHeadsURL;

    @Value("${ml.service.url.predictRoles}")
    private String predictRoles;

    public Double effortValue = (double) 0;

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
        String role= null;

        List<ProjectDTO> projects =  projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            role = jwtTokenProvider.getRole(token);
            String currentUserId = String.valueOf(jwtTokenProvider.getUserId(token));

            if (role != null && !role.equals("ADMIN")) {
                return projects.stream()
                        .filter(project -> project.getCreateUserId().toString().equals(currentUserId))
                        .collect(Collectors.toList());
            }
        }




        return projects;
    }

    @Override
    public List<ProjectDTO> getAllProjectsBudgetActive() {
        return projectRepository.findAll().stream()
                .filter(project -> project.getPredictedBudgetForResources() != null && project.getPredictedBudgetForResources().compareTo(BigDecimal.ZERO) > 0)
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

        existingProject.setPredictedBudgetForResources(projectDTO.getPredictedBudgetForResources());

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
    public EffortCombinedCallResponse saveAdvance(AdvanceDetailsDTO advanceDetailsDTO) {

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
        effortValue = mlResponse.getBody().getEffort();
        ResourceMLRequestDTO requestDto = new ResourceMLRequestDTO();
        requestDto.setDomain(advanceDetailsDTO.getDomain());
        requestDto.setMethodology(advanceDetailsDTO.getMethodology()==2?"Agile":"Waterfall");
        requestDto.setDatabaseType("SQL");
        requestDto.setUserCountEstimate(advanceDetailsDTO.getUserCount());
        requestDto.setProjectSize("Medium");
        requestDto.setDataSecurityLevel("Medium");
        requestDto.setExpectedDataSize(advanceDetailsDTO.getUserCount());
        requestDto.setCloudType("Private");
        requestDto.setIsMultiDB(0);
        requestDto.setIsMultiLang(1);
        requestDto.setMachineLearningEnabled(advanceDetailsDTO.getMl() ?1:0);
        requestDto.setIsDevOps(advanceDetailsDTO.getDevops()? 1:0);
        requestDto.setIntegrationRequired(advanceDetailsDTO.getIntegration()?1:0);
        HttpEntity<ResourceMLRequestDTO> entity2 = new HttpEntity<>(requestDto, headers);

        // Send the request to the ML service
        ResponseEntity<ResourceMLResponseDTO> mlResponse2 = restTemplate.exchange(
                resourceMLURL,
                HttpMethod.POST,
                entity2,
                ResourceMLResponseDTO.class
        );

        // Check the response status and handle errors
        if (!mlResponse2.getStatusCode().is2xxSuccessful() || mlResponse2.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " +
                    mlResponse2.getStatusCode());
        }

        // Save the response to the database
        ResourceMLResponseDTO responseDTO = mlResponse2.getBody();
        ProjectResourceConfig projectResourceConfig = new ProjectResourceConfig();
        projectResourceConfig.setProjectId(advanceDetailsDTO.getProjectId());
        projectResourceConfig.setCloud(responseDTO.getResourceCloud().isPrediction());
        projectResourceConfig.setDb(responseDTO.getResourceDB().isPrediction());
        projectResourceConfig.setAutomation(responseDTO.getResourceAutomation().isPrediction());
        projectResourceConfig.setSecurity(responseDTO.getResourceSecurity().isPrediction());
        projectResourceConfig.setCollaboration(responseDTO.getResourceCollaboration().isPrediction());
        projectResourceConfig.setIde(responseDTO.getResourceIdeTools().isPrediction());

        // Save using JPA repository
        projectResourceConfigRepository.save(projectResourceConfig);

//        ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(advanceDetailsDTO.getProjectId());
        List<PerfectEmployeeDTO> perfectEmployees = perfectEmployeeService.getAllPerfectEmployees();
        var distribution = calculateRoleDistribution(perfectEmployees);
        var calculationHeader = calculateReleaseDetails(advanceDetailsDTO);
        CalculateHeadRequestDTO requestDto2 = new CalculateHeadRequestDTO();

        requestDto2.setTotal_effort(calculationHeader.getTotalEffort());
        requestDto2.setTasks(calculationHeader.getTasks());
        requestDto2.setRole_distribution(calculationHeader.getRoleDistribution());
        requestDto2.setMax_story_points(calculationHeader.getMaxStoryPoints());
        requestDto2.setAvg_hours_per_story_point(calculationHeader.getAvgHoursPerStoryPoint());

        HttpEntity<CalculateHeadRequestDTO> entity3 = new HttpEntity<>(requestDto2, headers);


        ResponseEntity<CalculateMLResponseDTO> mlResponse3 = restTemplate.exchange(
                calculateHeadsURL,
                HttpMethod.POST,
                entity3,
                CalculateMLResponseDTO.class
        );

        if (!mlResponse3.getStatusCode().is2xxSuccessful() || mlResponse3.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " +
                    mlResponse3.getStatusCode());
        }
        saveCalculation(mlResponse3.getBody(),advanceDetailsDTO.getProjectId());
        EffortCombinedCallResponse combinedCallResponse = new EffortCombinedCallResponse();
        combinedCallResponse.setEffort(mlResponse.getBody());
        combinedCallResponse.setResources(mlResponse2.getBody());
        combinedCallResponse.setCalculateHeadRequest(mlResponse3.getBody());
        advanceDetailsRepository.save(convertToAdvanceModel(advanceDetailsDTO));

        return combinedCallResponse;
    }

    public ReleaseCalculationResponse calculateReleaseDetails(AdvanceDetailsDTO advanceDetailsDTO) {
        List<PerfectEmployeeDTO> perfectEmployees = perfectEmployeeService.getAllPerfectEmployees();
        ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(advanceDetailsDTO.getProjectId());
        RoleDistributionResponse distribution = calculateRoleDistribution(perfectEmployees);
        return buildResponse(releaseVersion, distribution);
    }

    private ReleaseCalculationResponse buildResponse(ReleaseVersionDTO releaseVersion, RoleDistributionResponse distribution) {
        ReleaseCalculationResponse response = new ReleaseCalculationResponse();
        response.setTotalEffort(effortValue);
        response.setTasks(calculateTaskCounts(releaseVersion));

        if (distribution != null) {
            response.setRoleDistribution(distribution.getRoleDistributions());
        }

        response.setMaxStoryPoints(calculateMaxStoryPoints(releaseVersion, distribution));
        response.setAvgHoursPerStoryPoint(1);
        return response;
    }

    private Map<String, Integer> calculateTaskCounts(ReleaseVersionDTO releaseVersion) {
        Map<String, Integer> taskCounts = new HashMap<>();

        if (releaseVersion != null && releaseVersion.getTasks() != null) {
            for (TaskDTO task : releaseVersion.getTasks()) {
                String roleCategory = String.valueOf(task.getRoleCategory());
                if (roleCategory != null) {
                    taskCounts.merge(roleCategory, 1, Integer::sum);

                    if (task.getSubTaskList() != null) {
                        for (SubTaskDTO subtask : task.getSubTaskList()) {
                            String subtaskCategory = String.valueOf(subtask.getRoleCategory());
                            if (subtaskCategory != null) {
                                taskCounts.merge(subtaskCategory, 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }

        for (RoleCategory category : RoleCategory.values()) {
            taskCounts.putIfAbsent(category.name(), 0);
        }

        return taskCounts;
    }

    private Map<String, Integer> calculateMaxStoryPoints(ReleaseVersionDTO releaseVersion,
                                                         RoleDistributionResponse distribution) {
        Map<String, Integer> maxPoints = new HashMap<>();
        Random random = new Random();

        if (distribution != null && distribution.getRoleDistributions() != null) {
            for (Map.Entry<String, Map<String, Double>> roleEntry : distribution.getRoleDistributions().entrySet()) {
                String roleCategory = roleEntry.getKey();

                Integer difficultyLevel = null;
                if (releaseVersion != null && releaseVersion.getTasks() != null) {
                    difficultyLevel = releaseVersion.getTasks().stream()
                            .filter(task -> roleCategory.equals(task.getRoleCategory()))
                            .map(TaskDTO::getDifficultyLevel)
                            .filter(diff -> diff != null)
                            .findFirst()
                            .orElse(null);
                }

                if (difficultyLevel == null) {
                    difficultyLevel = random.nextInt(7) + 1;
                }

                for (String roleName : roleEntry.getValue().keySet()) {
                    int points;
                    String lowerRoleName = roleName.toLowerCase();
                    if (lowerRoleName.contains("senior") ||
                            lowerRoleName.contains("sinior") ||
                            lowerRoleName.contains("tech lead")) {
                        points = Math.min(difficultyLevel + 4, 8);
                    } else if (lowerRoleName.contains("intern") ||
                            lowerRoleName.contains("associate")) {
                        points = Math.max(difficultyLevel - 2, 3);
                    } else {
                        points = Math.min(difficultyLevel + 2, 6);
                    }
                    maxPoints.put(roleName, points);
                }
            }
        }

        return maxPoints;
    }



    public RoleDistributionResponse calculateRoleDistribution(List<PerfectEmployeeDTO> employees) {
        // Convert EmployeeDTO list to CategoryRole list
        List<CategoryRole> roles = employees.stream()
                .map(CategoryRole::fromEmployeeDTO)
                .collect(Collectors.toList());

        RoleDistributionResponse response = new RoleDistributionResponse();
        Map<String, Map<String, Double>> distributions = new HashMap<>();

        // Group roles by category
        Map<String, List<CategoryRole>> rolesByCategory = roles.stream()
                .collect(Collectors.groupingBy(CategoryRole::getRoleCategory));

        rolesByCategory.forEach((category, categoryRoles) -> {
            Map<String, Double> roleDistribution = new HashMap<>();

            // Calculate distribution for each role in category
            categoryRoles.forEach(role -> {
                double distribution = role.getRoleDistributionValue() == 0
                        ? 0.4  // Default value if roleDistributionValue is 0
                        : role.getRoleDistributionValue() / 100.0;

                roleDistribution.put(role.getRoleName(), distribution);
            });

            // Normalize distribution values if needed
            normalizeDistribution(roleDistribution);

            distributions.put(category, roleDistribution);
        });

        response.setRoleDistributions(distributions);
        return response;
    }

    private void normalizeDistribution(Map<String, Double> distribution) {
        double total = distribution.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total != 1.0 && !distribution.isEmpty()) {
            distribution.forEach((key, value) ->
                    distribution.put(key, value / total));
        }
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
        dto.setPredictedBudget(project.getPredictedBudgetForResources());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        dto.setReleaseVersions(releaseVersionService.getReleaseVersionsByProjectIdNew(project.getId()));
        return dto;
    }

    public void saveCalculation(CalculateMLResponseDTO dto, Long projectId) {
        CalculationResult result = new CalculationResult();
        result.setProjectId(projectId);

        dto.getCategories().forEach((categoryName, categoryDto) -> {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCategoryName(categoryName);
            categoryEntity.setCalculationResult(result);

            categoryDto.getSubCategories().forEach((subCategoryName, effortDto) -> {
                EffortDetailsEntity effortEntity = new EffortDetailsEntity();
                effortEntity.setSubCategoryName(subCategoryName);
                effortEntity.setEffort(effortDto.getEffort());
                effortEntity.setHeadsNeeded(effortDto.getHeads_needed());
                effortEntity.setStoryPoints(effortDto.getStory_points());
                effortEntity.setCategory(categoryEntity);

                categoryEntity.getSubCategories().put(subCategoryName, effortEntity);
            });

            result.getCategories().put(categoryName, categoryEntity);
        });

        calculationResultRepository.save(result);
    }

}
