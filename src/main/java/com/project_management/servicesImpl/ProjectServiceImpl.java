package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.enums.ProjectStatus;
import com.project_management.models.enums.RoleCategory;
import com.project_management.repositories.AdvanceDetailsRepository;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ProjectResourceConfigRepository;
import com.project_management.services.PerfectEmployeeService;
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
    RestTemplate restTemplate;

    @Value("${ml.service.url.effort}")
    private String effortURL;

    @Value("${ml.service.url.resources}")
    private String resourceMLURL;

    @Value("${ml.service.url.calculateHeads}")
    private String calculateHeadsURL;

    @Value("${ml.service.url.predictRoles}")
    private String predictRoles;

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

        ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(advanceDetailsDTO.getProjectId());
        TaskAnalyticsResponseDTO requestBody = new TaskAnalyticsResponseDTO();

        Map<String, List<TaskCountByPriorityDTO>> tasksByRole = new HashMap<>();
        for (RoleCategory role : RoleCategory.values()) {
            tasksByRole.put(role.name().toLowerCase(), new ArrayList<>());
        }

        List<TaskDTO> tasks = releaseVersion.getTasks();
        Map<String, Map<String, Long>> countByRoleAndPriority = new HashMap<>();

        for (TaskDTO task : tasks) {
            String roleCategory = task.getRoleCategory().toString().toLowerCase();
            String priority = task.getPriorityLevel().toString().toLowerCase();

            countByRoleAndPriority.computeIfAbsent(roleCategory, k -> new HashMap<>());
            Map<String, Long> priorityCount = countByRoleAndPriority.get(roleCategory);
            priorityCount.merge(priority, 1L, Long::sum);

            if (task.getSubTaskList() != null) {
                for (SubTaskDTO subTask : task.getSubTaskList()) {
                    String subTaskRole = subTask.getRoleCategory().toString().toLowerCase();
                    countByRoleAndPriority.computeIfAbsent(subTaskRole, k -> new HashMap<>());
                    Map<String, Long> subTaskPriorityCount = countByRoleAndPriority.get(subTaskRole);
                    subTaskPriorityCount.merge(priority, 1L, Long::sum);
                }
            }
        }

        for (Map.Entry<String, Map<String, Long>> roleEntry : countByRoleAndPriority.entrySet()) {
            String role = roleEntry.getKey();
            List<TaskCountByPriorityDTO> priorityCounts = new ArrayList<>();

            for (Map.Entry<String, Long> priorityEntry : roleEntry.getValue().entrySet()) {
                TaskCountByPriorityDTO countByPriority = new TaskCountByPriorityDTO();
                countByPriority.setCount(priorityEntry.getValue());
                countByPriority.setPriority(priorityEntry.getKey());
                priorityCounts.add(countByPriority);
            }

            tasksByRole.put(role, priorityCounts);
        }

        Iterator<Map.Entry<String, List<TaskCountByPriorityDTO>>> iterator = tasksByRole.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<TaskCountByPriorityDTO>> entry = iterator.next();
            if (entry.getValue().isEmpty()) {
                iterator.remove();
            }
        }

        requestBody.setTasks(tasksByRole);
        requestBody.setEffort(100);

        List<String> roles = Arrays.stream(RoleCategory.values())
                .map(role -> role.name().toLowerCase())
                .collect(Collectors.toList());
        requestBody.setRoles(roles);


        HttpEntity<TaskAnalyticsResponseDTO> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<TaskAnalyticsResponseDTO> mlResponse3 = restTemplate.exchange(
                predictRoles,
                HttpMethod.POST,
                requestEntity,
                TaskAnalyticsResponseDTO.class
        );

        if (!mlResponse3.getStatusCode().is2xxSuccessful() || mlResponse3.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " + mlResponse3.getStatusCode());
        }

        if(mlResponse3.getBody().getRoles().isEmpty()){
            mlResponse3.getBody().setRoles(roles);
        }
        EffortResponse effortResponse = calculateEffort(advanceDetailsDTO.getProjectId());
        // Validate and ensure all task types have role distributions
        effortResponse = prepareEffortResponse(effortResponse);

        HttpEntity<EffortResponse> requestEntity2 = new HttpEntity<>(effortResponse, headers);

        ResponseEntity<CalculateHeadRequestDTO> mlResponse4 = restTemplate.exchange(
                calculateHeadsURL,
                HttpMethod.POST,
                requestEntity2,
                CalculateHeadRequestDTO.class
        );

        if (!mlResponse4.getStatusCode().is2xxSuccessful() || mlResponse4.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " + mlResponse4.getStatusCode());
        }

        EffortCombinedCallResponse combinedCallResponse = new EffortCombinedCallResponse();
        combinedCallResponse.setEffort(mlResponse.getBody());
        combinedCallResponse.setResources(mlResponse2.getBody());
        combinedCallResponse.setRoles(mlResponse3.getBody().getRoles());
        combinedCallResponse.setCalculateHeadRequest(mlResponse4.getBody());
        advanceDetailsRepository.save(convertToAdvanceModel(advanceDetailsDTO));

        return combinedCallResponse;
    }

    public EffortResponse calculateEffort(Long projectId) {
        EffortResponse response = new EffortResponse();

        // Get release version data including role distributions
        ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(projectId);

        // Set role distributions from the service response
        response.setRoleDistribution(calculateRoleDistribution(perfectEmployeeService.getAllPerfectEmployees()));

        // Calculate tasks count from release version
        Map<String, Integer> tasksCount = calculateTasksCount(releaseVersion);
        response.setTasks(tasksCount);

        // Calculate max story points based on difficulty levels
        Map<String, Integer> storyPoints = calculateMaxStoryPoints(releaseVersion);
        response.setMaxStoryPoints(storyPoints);

        return response;
    }

    private Map<String, Integer> calculateTasksCount(ReleaseVersionDTO releaseVersion) {
        Map<String, Integer> tasksCount = new HashMap<>();

        // Initialize counters for each role category
        Arrays.stream(RoleCategory.values())
                .forEach(role -> tasksCount.put(role.name().toLowerCase(), 0));

        // Count main tasks
        releaseVersion.getTasks().forEach(task -> {
            String roleCategory = task.getRoleCategory().toString().toLowerCase();
            tasksCount.merge(roleCategory, 1, Integer::sum);

            // Count subtasks
            if (task.getSubTaskList() != null) {
                task.getSubTaskList().forEach(subTask -> {
                    String subTaskRole = subTask.getRoleCategory().toString().toLowerCase();
                    tasksCount.merge(subTaskRole, 1, Integer::sum);
                });
            }
        });

        return tasksCount;
    }

    private Map<String, Integer> calculateMaxStoryPoints(ReleaseVersionDTO releaseVersion) {
        Map<String, Integer> storyPoints = new HashMap<>();

        Map<String, Map<String, Double>> roleDistribution = calculateRoleDistribution(perfectEmployeeService.getAllPerfectEmployees());

        // Iterate through each role category from the distribution data
        roleDistribution.forEach((roleCategory, subRoles) -> {
            // For each task that matches this role category
            releaseVersion.getTasks().stream()
                    .filter(task -> roleCategory.equals(task.getRoleCategory().toString().toLowerCase()))
                    .forEach(task -> {
                        // Get the difficulty level, use default values if null
                        Integer difficultyLevel = task.getDifficultyLevel();

                        // Update story points for each sub-role
                        subRoles.keySet().forEach(subRole -> {
                            if (difficultyLevel != null) {
                                storyPoints.put(subRole, difficultyLevel);
                            } else {
                                // Use default values when difficultyLevel is null
                                int defaultValue = getDefaultStoryPoints(subRole);
                                storyPoints.put(subRole, defaultValue);
                            }
                        });
                    });
        });

        return storyPoints;
    }

    public Map<String, Map<String, Double>> calculateRoleDistribution(List<PerfectEmployeeDTO> employees) {
        Map<String, Map<String, Double>> distribution = new HashMap<>();

        // Group employees by roleCategory
        Map<String, List<PerfectEmployeeDTO>> categoryGroups = employees.stream()
                .collect(Collectors.groupingBy(
                        employee -> employee.getRoleCategory().toString().toLowerCase(),
                        Collectors.toList()
                ));

        // Process each category
        categoryGroups.forEach((category, categoryEmployees) -> {
            // Group employees by roleShortName within category
            Map<String, List<PerfectEmployeeDTO>> roleGroups = categoryEmployees.stream()
                    .collect(Collectors.groupingBy(
                            PerfectEmployeeDTO::getRoleShortName,  // Changed this line
                            Collectors.toList()
                    ));

            Map<String, Double> roleDistributions = new HashMap<>();

            // Calculate distributions for each role
            roleGroups.forEach((roleShortName, roleEmployees) -> {
                // Get the distribution value from the first employee with non-zero distribution
                Optional<PerfectEmployeeDTO> employeeWithDistribution = roleEmployees.stream()
                        .filter(e -> e.getRoleDistributionValue() > 0)
                        .findFirst();

                double distributionValue = employeeWithDistribution
                        .map(e -> e.getRoleDistributionValue() / 100.0)
                        .orElse(0.4); // Default value if no distribution is set

                roleDistributions.put(roleShortName, distributionValue);
            });

            distribution.put(category, roleDistributions);
        });

        return distribution;
    }

    public EffortResponse prepareEffortResponse(EffortResponse effortResponse) {
        // Get all task types from the tasks map
        Set<String> taskTypes = effortResponse.getTasks().keySet();

        // Ensure each task type has an entry in role_distribution
        Map<String, Map<String, Double>> roleDistribution = effortResponse.getRoleDistribution();

        for (String taskType : taskTypes) {
            // If this task type doesn't have a role distribution, add a default one
            if (!roleDistribution.containsKey(taskType)) {
                Map<String, Double> defaultRoleDistribution = new HashMap<>();
                // Add your default roles and percentages
                defaultRoleDistribution.put("dev", 0.7);
                defaultRoleDistribution.put("qa", 0.3);

                roleDistribution.put(taskType, defaultRoleDistribution);
            }
        }

        effortResponse.setRoleDistribution(roleDistribution);
        return effortResponse;
    }

    private int getDefaultStoryPoints(String role) {
        // Default values as specified in the requirements
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("sse", 7);
        defaults.put("se", 5);
        defaults.put("ase", 3);
        defaults.put("sqa", 7);
        defaults.put("qa", 5);
        defaults.put("aqa", 3);
        defaults.put("sde", 8);
        defaults.put("de", 6);
        defaults.put("ade", 4);
        defaults.put("sba", 7);
        defaults.put("ba", 4);

        return defaults.getOrDefault(role, 5); // Default to 5 if role not found
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

}
