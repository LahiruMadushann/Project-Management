package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.constant.PriorityValue;
import com.project_management.models.enums.PriorityLevel;
import com.project_management.models.enums.RoleCategory;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.*;
import com.project_management.services.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskCreationFromMLService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    // TaskCreationFromMLService.java
    @Transactional
    public List<TaskDTO> createTasksFromMLAnalysis(
            MLAnalysisResponse mlAnalysis,
            Long releaseVersionId,
            Long createUserId,
            Integer difficultyLevel,
            LocalDate assignedDate,
            LocalDate startDate,
            LocalDate deadline,
            LocalDate completedDate
    ) {
        List<TaskDTO> createdTasks = new ArrayList<>();

        ReleaseVersion releaseVersion = releaseVersionRepository.findById(releaseVersionId)
                .orElseThrow(() -> new RuntimeException("Release Version not found"));

        User createUser = userRepository.findById(createUserId)
                .orElseThrow(() -> new RuntimeException("Create User not found"));

        // Sort tasks by priority
        List<TaskDetail> allTaskDetails = mlAnalysis.getResults().stream()
                .flatMap(taskBreakdown -> taskBreakdown.getTasks().stream())
                .sorted(Comparator.comparing(taskDetail -> {
                    if (taskDetail.getPriorityLevel() == null) {
                        return PriorityLevel.MEDIUM; // Default priority
                    }
                    return taskDetail.getPriorityLevel();
                }, Comparator.nullsFirst(Comparator.reverseOrder()))) // HIGH -> MEDIUM -> LOW
                .collect(Collectors.toList());

        Set<Long> assignedUsers = new HashSet<>();
        for (TaskDetail taskDetail : allTaskDetails) {
            if (releaseVersion.getVersionLimitConstant() == 0) {
                // Create a new release version if the limit is reached
                releaseVersion = createNewReleaseVersion(releaseVersion, createUserId);
            }
            Long projectId = releaseVersion.getProject().getId();
            // Create main task
            Task mainTask = new Task();
            mainTask.setName(taskDetail.getTaskName());
            mainTask.setStatus(TaskStatus.TODO);
            mainTask.setReleaseVersion(releaseVersion);
            mainTask.setCreateUserId(createUser.getId());
            mainTask.setDifficultyLevel(difficultyLevel);
            mainTask.setAssignedDate(assignedDate);
            mainTask.setStartDate(startDate);
            mainTask.setDeadline(deadline);
            mainTask.setCompletedDate(completedDate);
            mainTask.setCreatedAt(LocalDateTime.now());
            mainTask.setUpdatedAt(LocalDateTime.now());

            // Set priority level
            if (taskDetail.getPriorityLevel() == null) {
                mainTask.setPriorityLevel(PriorityLevel.MEDIUM); // Default priority
            } else {
                mainTask.setPriorityLevel(taskDetail.getPriorityLevel());
            }

            // Save the main task first
            Task savedMainTask = taskRepository.save(mainTask);

            // Create and save subtasks with role-based assignment
            List<SubTask> subtasks = new ArrayList<>();
            User mainTaskAssignedUser = null;
            int maxSubtaskWeight = 0;

            for (SubTaskDetail subTaskDetail : taskDetail.getSubtasks()) {
                SubTask subTask = new SubTask();
                subTask.setName(subTaskDetail.getSubtaskName());
                subTask.setStatus(TaskStatus.TODO);

                try {
                    // Convert ML tag to RoleCategory
                    String roleTag = subTaskDetail.getTag().trim().toLowerCase();
                    RoleCategory requiredRole = RoleCategory.valueOf(roleTag);
                    subTask.setTags(roleTag);

                    // First, try to find employees with exact difficulty match and role
                    List<Employee> exactMatches = employeeRepository.findTeamEmployees(projectId).stream()
                            .filter(emp -> emp.getRoleCategory() == requiredRole &&
                                    emp.getDifficultyLevel() == difficultyLevel &&
                                    emp.getUser() != null && !assignedUsers.contains(emp.getUser().getId()))
                            .collect(Collectors.toList());

                    User assignedUser = null;
                    Employee selectedEmployee = null;

                    if (!exactMatches.isEmpty()) {
                        // Randomly select from exact matches to distribute work
                        selectedEmployee = exactMatches.get(new Random().nextInt(exactMatches.size()));
                    } else {
                        // If no exact matches, find closest match employees
                        List<Employee> closestMatches = employeeRepository.findTeamEmployees(projectId).stream()
                                .filter(emp -> emp.getRoleCategory() == requiredRole &&
                                        emp.getUser() != null && !assignedUsers.contains(emp.getUser().getId()))
                                .sorted(Comparator.comparingInt(emp ->
                                        Math.abs(emp.getDifficultyLevel() - difficultyLevel)))
                                .limit(5) // Take top 5 closest matches
                                .collect(Collectors.toList());

                        if (!closestMatches.isEmpty()) {
                            selectedEmployee = closestMatches.get(0);
                            log.info("No exact match found. Selected closest match for subtask '{}': employee '{}' with difficulty level {}",
                                    subTaskDetail.getSubtaskName(),
                                    selectedEmployee.getEmployeeName(),
                                    selectedEmployee.getDifficultyLevel());
                        } else {
                            log.warn("No suitable employees found for subtask '{}' with role '{}'",
                                    subTaskDetail.getSubtaskName(), requiredRole);
                        }
                    }

                    // Assign user if an employee is found
                    if (selectedEmployee != null) {
                        assignedUser = selectedEmployee.getUser();

                        // Track the user for main task assignment based on subtask complexity
                        int subtaskWeight = (int) Math.ceil(subTaskDetail.getEstimatedHours());
                        if (subtaskWeight > maxSubtaskWeight) {
                            mainTaskAssignedUser = assignedUser;
                            maxSubtaskWeight = subtaskWeight;
                        }

                        log.info("Assigned subtask '{}' to employee '{}' with role '{}'",
                                subTaskDetail.getSubtaskName(),
                                selectedEmployee.getEmployeeName(),
                                selectedEmployee.getRoleCategory());

                        // Add the user to the assigned set to avoid consecutive assignment
                        assignedUsers.add(assignedUser.getId());
                    }

                    subTask.setTask(savedMainTask);
                    subTask.setCreateUserId(createUserId);
                    subTask.setAssignedUser(assignedUser);
                    subTask.setAssignedDate(LocalDate.now());
                    subTask.setStartDate(LocalDate.now());
                    subTask.setDeadline(LocalDate.now().plusDays(
                            (long) Math.ceil(subTaskDetail.getEstimatedHours() / 8.0)
                    ));
                    subTask.setCreatedAt(LocalDateTime.now());
                    subTask.setUpdatedAt(LocalDateTime.now());

                    subtasks.add(subTask);

                } catch (IllegalArgumentException e) {
                    log.error("Invalid role category from ML response: {}", subTaskDetail.getTag());
                    throw new RuntimeException("Invalid role category: " + subTaskDetail.getTag());
                }
            }

            // Assign main task to the user with the most complex subtask
            savedMainTask.setAssignedUser(mainTaskAssignedUser);

            // Save all subtasks
            List<SubTask> savedSubtasks = subTaskRepository.saveAll(subtasks);
            savedMainTask.setSubTasks(savedSubtasks);

            // Save the updated main task
            Task finalSavedTask = taskRepository.save(savedMainTask);
            createdTasks.add(convertTaskToDTO(finalSavedTask));

            // Reduce the version limit constant
            releaseVersion.reduceLimit(difficultyLevel);
        }

        return createdTasks;
    }

    public List<TaskDTO> assignAutoUsers(Long projectId) {
        List<TaskDTO> existingTasks = taskService.getAllTasks();
        List<TaskDetail> allTaskDetails = existingTasks.stream().map((task) -> {
            TaskDetail taskDetail = new TaskDetail();
            taskDetail.setTaskName(task.getName());
            taskDetail.setPriorityLevel(task.getPriorityLevel());
            taskDetail.setEstimatedHours(1);
            taskDetail.setSubtasks(task.getSubTaskList().stream().map((subTask) -> {
                SubTaskDetail subTaskDetail = new SubTaskDetail();
                subTaskDetail.setSubtaskName(subTask.getName());
                subTaskDetail.setTag(subTask.getTags());
                subTaskDetail.setEstimatedHours(1);
                return subTaskDetail;
            }).collect(Collectors.toList()));
            return taskDetail;
        }).toList();

        Set<Long> assignedUsers = new HashSet<>();

        for (int i = 0; i < existingTasks.size(); i++) {
            TaskDTO existingTaskDTO = existingTasks.get(i);
            TaskDetail taskDetail = allTaskDetails.get(i);

            // Fetch existing task from repository
            Task existingTask = taskRepository.findById(existingTaskDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + existingTaskDTO.getId()));

            User mainTaskAssignedUser = null;
            int maxSubtaskWeight = 0;

            // Update subtasks
            for (int j = 0; j < existingTask.getSubTasks().size(); j++) {
                SubTask existingSubTask = existingTask.getSubTasks().get(j);
                SubTaskDetail subTaskDetail = taskDetail.getSubtasks().get(j);

                try {
                    String roleTag = subTaskDetail.getTag().trim().toLowerCase();
                    RoleCategory requiredRole = RoleCategory.valueOf(roleTag);

                    // Find suitable employee
                    List<Employee> exactMatches = employeeRepository.findTeamEmployees(projectId).stream()
                            .filter(emp -> emp.getRoleCategory() == requiredRole &&
                                    Objects.equals(emp.getDifficultyLevel(), existingTask.getDifficultyLevel()) &&
                                    emp.getUser() != null && !assignedUsers.contains(emp.getUser().getId()))
                            .collect(Collectors.toList());

                    User assignedUser = null;
                    Employee selectedEmployee = null;

                    if (!exactMatches.isEmpty()) {
                        selectedEmployee = exactMatches.get(new Random().nextInt(exactMatches.size()));
                    } else {
                        List<Employee> closestMatches = employeeRepository.findTeamEmployees(projectId).stream()
                                .filter(emp -> emp.getRoleCategory() == requiredRole &&
                                        emp.getUser() != null && !assignedUsers.contains(emp.getUser().getId()))
                                .sorted(Comparator.comparingInt(emp ->
                                        Math.abs(emp.getDifficultyLevel() - existingTask.getDifficultyLevel())))
                                .limit(5)
                                .collect(Collectors.toList());

                        if (!closestMatches.isEmpty()) {
                            selectedEmployee = closestMatches.get(0);
                            log.info("No exact match found. Selected closest match for subtask '{}': employee '{}' with difficulty level {}",
                                    subTaskDetail.getSubtaskName(),
                                    selectedEmployee.getEmployeeName(),
                                    selectedEmployee.getDifficultyLevel());
                        } else {
                            log.warn("No suitable employees found for subtask '{}' with role '{}'",
                                    subTaskDetail.getSubtaskName(), requiredRole);
                        }
                    }

                    if (selectedEmployee != null) {
                        assignedUser = selectedEmployee.getUser();

                        int subtaskWeight = (int) Math.ceil(subTaskDetail.getEstimatedHours());
                        if (subtaskWeight > maxSubtaskWeight) {
                            mainTaskAssignedUser = assignedUser;
                            maxSubtaskWeight = subtaskWeight;
                        }

                        log.info("Assigned subtask '{}' to employee '{}' with role '{}'",
                                subTaskDetail.getSubtaskName(),
                                selectedEmployee.getEmployeeName(),
                                selectedEmployee.getRoleCategory());

                        assignedUsers.add(assignedUser.getId());

                        existingSubTask.setAssignedUser(assignedUser);
                        existingSubTask.setUpdatedAt(LocalDateTime.now());
                    }

                } catch (IllegalArgumentException e) {
                    log.error("Invalid role category from ML response: {}", subTaskDetail.getTag());
                    throw new RuntimeException("Invalid role category: " + subTaskDetail.getTag());
                }
            }

            existingTask.setAssignedUser(mainTaskAssignedUser);
            existingTask.setUpdatedAt(LocalDateTime.now());

            taskRepository.save(existingTask);
        }

        return taskService.getAllTasks();
    }

    // Create a new release version
    private ReleaseVersion createNewReleaseVersion(ReleaseVersion oldVersion, Long createUserId) {
        ReleaseVersion newVersion = new ReleaseVersion();
        newVersion.setProject(oldVersion.getProject());
        newVersion.setVersionName(oldVersion.getVersionName() + "-new-"+ LocalDateTime.now());
        newVersion.setCreateUserId(createUserId);
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setUpdatedAt(LocalDateTime.now());
        newVersion.setVersionDescription(oldVersion.getVersionDescription());
        newVersion.setVersionLimitConstant(PriorityValue.PRIORITY_VALUE);
        return releaseVersionRepository.save(newVersion);
    }


    @Transactional
    public List<TaskDTO> createTasksFromStories(CreateTasksFromStoriesRequest request) {
        try {
            // Create request body exactly matching Python API expectations
            if (request.getDifficultyLevel() == null) {
                LocalDate deadline = request.getDeadline();
                if(deadline !=null ||  request != null ){
                    List<Integer> levels = Arrays.asList(1 << 0, 1 << 1 | 1, 1 << 2 | 1, 1 << 2 | 1 << 1 | 1);
                    Collections.shuffle(levels, new Random(System.currentTimeMillis()));
                    request.setDifficultyLevel(levels.get(0));
                }
                request.setDifficultyLevel(3);
            }


            Map<String, List<Map<String, String>>> mlRequest = new HashMap<>();
            List<Map<String, String>> stories = request.getSimpleUserStories().stream()
                    .map(story -> {
                        Map<String, String> storyMap = new HashMap<>();
                        storyMap.put("user_type", story.getUserType().toLowerCase());
                        storyMap.put("action", story.getAction().toLowerCase());
                        storyMap.put("what", story.getWhat().toLowerCase());
                        storyMap.put("context", story.getContext().toLowerCase());
                        return storyMap;
                    })
                    .collect(Collectors.toList());

            mlRequest.put("simple_user_stories", stories);

            // Add debug logging
            log.debug("Sending request to ML service: {}", mlRequest);

            // Make HTTP request with proper headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, List<Map<String, String>>>> entity =
                    new HttpEntity<>(mlRequest, headers);

            ResponseEntity<MLAnalysisResponse> mlResponse = restTemplate.exchange(
                    mlServiceUrl,
                    HttpMethod.POST,
                    entity,
                    MLAnalysisResponse.class
            );

            if (!mlResponse.getStatusCode().is2xxSuccessful() || mlResponse.getBody() == null) {
                throw new RuntimeException("Failed to get analysis from ML service: " +
                        mlResponse.getStatusCode());
            }

            // Create tasks using the ML analysis
            return createTasksFromMLAnalysis(
                    mlResponse.getBody(),
                    request.getReleaseVersionId(),
                    request.getCreateUserId(),
                    request.getDifficultyLevel(),
                    request.getAssignedDate(),
                    request.getStartDate(),
                    request.getDeadline(),
                    request.getCompletedDate()
            );

        } catch (HttpStatusCodeException e) {
            log.error("ML service error response: {}", e.getResponseBodyAsString());
            throw new RuntimeException("ML service error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error calling ML service: ", e);
            throw new RuntimeException("Failed to process user stories: " + e.getMessage(), e);
        }
    }




private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setName(task.getName());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setTags(task.getTags());
        //taskDTO.setRoleCategory(RoleCategory.valueOf(task.getTags()));
    taskDTO.setPriorityLevel(task.getPriorityLevel());
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());
        taskDTO.setCreateUserId(task.getCreateUserId());
        taskDTO.setDifficultyLevel(task.getDifficultyLevel());
        if (task.getAssignedUser() != null) {
            taskDTO.setAssignedUserId(task.getAssignedUser().getId());
        }
        taskDTO.setAssignedDate(task.getAssignedDate());
        taskDTO.setStartDate(task.getStartDate());
        taskDTO.setDeadline(task.getDeadline());
        taskDTO.setCompletedDate(task.getCompletedDate());

        // Convert subtasks
        if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
            taskDTO.setSubTaskList(task.getSubTasks().stream()
                    .map(this::convertSubTaskToDTO)
                    .collect(Collectors.toList()));
        }

        return taskDTO;
    }

    private SubTaskDTO convertSubTaskToDTO(SubTask subTask) {
        SubTaskDTO subTaskDTO = new SubTaskDTO();
        subTaskDTO.setId(subTask.getId());
        subTaskDTO.setTaskId(subTask.getTask().getId());
        subTaskDTO.setName(subTask.getName());
        subTaskDTO.setStatus(subTask.getStatus());
        subTaskDTO.setTags(subTask.getTags());
        //subTaskDTO.setRoleCategory(RoleCategory.valueOf(subTask.getTags()));
        subTaskDTO.setCreateUserId(subTask.getCreateUserId());
        if (subTask.getAssignedUser() != null) {
            subTaskDTO.setAssignedUserId(String.valueOf(subTask.getAssignedUser().getId()));
        }
        subTaskDTO.setAssignedDate(subTask.getAssignedDate());
        subTaskDTO.setStartDate(subTask.getStartDate());
        subTaskDTO.setDeadline(subTask.getDeadline());
        subTaskDTO.setCompletedDate(subTask.getCompletedDate());
        return subTaskDTO;
    }
}