package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.enums.PriorityLevel;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.*;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.SubTaskService;
import com.project_management.services.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SubTaskService subTaskService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.critical.url}")
    private String criticalUrl;




    @Override
    public TaskDTO createTask(TaskDTO taskDTO) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(taskDTO.getReleaseVersionId())
                .orElseThrow(() -> new NoSuchElementException("Release Version not found"));

        Task task = new Task();
        BeanUtils.copyProperties(taskDTO, task, "id", "releaseVersion", "assignedUser");

        // Find suitable employee based on difficulty level
        if (taskDTO.getDifficultyLevel() != null) {
            Long projectId = releaseVersion.getProject().getId();

            // First try to find exact matches
            List<Employee> exactMatches = employeeRepository.findSuitableEmployees(
                    taskDTO.getDifficultyLevel(),
                    projectId
            );

            Optional<Employee> selectedEmployee = employeeRepository.findByUserId(taskDTO.getAssignedUserId());

//            Employee selectedEmployee;
//            if (!exactMatches.isEmpty()) {
//                // If there are exact matches, randomly select one to distribute work evenly
//                int randomIndex = (int) (Math.random() * exactMatches.size());
//                selectedEmployee = exactMatches.get(randomIndex);
//            } else {
//                // If no exact matches, find the closest match
//                List<Employee> closestMatches = employeeRepository.findClosestMatchEmployees(
//                        taskDTO.getDifficultyLevel(),
//                        projectId
//                );
//
//                if (closestMatches.isEmpty()) {
//                    throw new NoSuchElementException("No suitable employees found for this task");
//                }
//
//                selectedEmployee = closestMatches.get(0); // Get the closest match
//            }
            if (selectedEmployee.isPresent()) {
                User assignedUser = selectedEmployee.get().getUser();
                task.setAssignedUser(assignedUser);
                task.setAssignedDate(LocalDate.now());
                task.setPriorityLevel(PriorityLevel.MEDIUM);
            } else {
                User assignedUser = null;
                task.setAssignedUser(assignedUser);
                task.setAssignedDate(LocalDate.now());
                task.setPriorityLevel(PriorityLevel.MEDIUM);
            }

        }

        task.setReleaseVersion(releaseVersion);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDTO(task);
    }

    @Override
    public CriticalPathResponse getTaskByProjectId(Long id) {
//        List<TaskDTO> tasks = new ArrayList<>();
//        List<ReleaseVersion> releaseVersions = releaseVersionRepository.findByProjectId(id);
//        releaseVersions.forEach(releaseVersion -> {
//            List<Task> temp = taskRepository.findByReleaseVersionId(releaseVersion.getId());
//            temp.forEach(task -> {
//                tasks.add(convertToDTO(task));
//            });
//        });
//        CriticalPathRequestDto requestDto = new CriticalPathRequestDto();
//        requestDto.setTasks(tasks);
//
//        // Prepare headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<CriticalPathRequestDto> entity = new HttpEntity<>(requestDto, headers);
//
//        // Send the request to the ML service
//        ResponseEntity<CriticalPathResponse> mlResponse = restTemplate.exchange(
//                criticalUrl,
//                HttpMethod.POST,
//                entity,
//                CriticalPathResponse.class
//        );
//
//        // Check the response status and handle errors
//        if (!mlResponse.getStatusCode().is2xxSuccessful() || mlResponse.getBody() == null) {
//            throw new RuntimeException("Failed to get prediction from ML service: " +
//                    mlResponse.getStatusCode());
//        }

        return null;
    }

    @Override
    public List<TaskDTO> getTasksByReleaseVersionId(Long releaseVersionId) {
        ReleaseVersion releaseVersion = releaseVersionRepository.findById(releaseVersionId)
                .orElseThrow(() -> new RuntimeException("Release Version not found"));

        return taskRepository.findByReleaseVersion(releaseVersion).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTONew> getTasksByReleaseVersionIdNew(Long releaseVersionId) {
        return taskRepository.findByReleaseVersionId(releaseVersionId).stream()
                .map(this::convertToDTONew)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        String role= null;
        List<TaskDTO> tasks = taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = (String) authentication.getCredentials();
            role = jwtTokenProvider.getRole(token);
            String currentUserId = String.valueOf(jwtTokenProvider.getUserId(token));

//            if (role != null && !role.equals("ADMIN")) {
//                return tasks.stream()
//                        .filter(task -> Optional.ofNullable(task.getAssignedUserId())
//                                .map(id -> id.toString().equals(currentUserId))
//                                .orElse(false))
//                        .collect(Collectors.toList());
//
//            }
        }
        return tasks;
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (taskDTO.getName() != null) {
            existingTask.setName(taskDTO.getName());
        }

        if (taskDTO.getStatus() != null) {
            existingTask.setStatus(taskDTO.getStatus());
        }

        if (taskDTO.getTags() != null) {
            existingTask.setTags(taskDTO.getTags());
        }

        if (taskDTO.getAssignedDate() != null) {
            existingTask.setAssignedDate(taskDTO.getAssignedDate());
        }

        if (taskDTO.getStartDate() != null) {
            existingTask.setStartDate(taskDTO.getStartDate());
        }

        if (taskDTO.getDeadline() != null) {
            existingTask.setDeadline(taskDTO.getDeadline());
        }

        if (taskDTO.getCompletedDate() != null) {
            existingTask.setCompletedDate(taskDTO.getCompletedDate());
        }

        if (taskDTO.getReleaseVersionId() != null &&
                !taskDTO.getReleaseVersionId().equals(existingTask.getReleaseVersion().getId())) {
            ReleaseVersion newVersion = releaseVersionRepository.findById(taskDTO.getReleaseVersionId())
                    .orElseThrow(() -> new RuntimeException("Release Version not found"));
            existingTask.setReleaseVersion(newVersion);
        }

        if (taskDTO.getAssignedUserId() != null &&
                (existingTask.getAssignedUser() == null || !taskDTO.getAssignedUserId().equals(existingTask.getAssignedUser().getId()))) {
            User newAssignedUser = userRepository.findById(taskDTO.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned User not found"));
            existingTask.setAssignedUser(newAssignedUser);
        }

        existingTask.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public List<TaskDTO> createTasksFromMLAnalysis(
            MLAnalysisResponse mlAnalysis,
            Long releaseVersionId,
            Long createUserId,
            Integer difficultyLevel,
            Long assignedUserId,
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

        User assignedUser = userRepository.findById(assignedUserId)
                .orElseThrow(() -> new RuntimeException("Assigned User not found"));

        for (TaskBreakdown taskBreakdown : mlAnalysis.getResults()) {
            // Create main task
            TaskDTO mainTaskDTO = new TaskDTO();
            mainTaskDTO.setName(taskBreakdown.getMainTask());
            mainTaskDTO.setStatus(TaskStatus.TODO);
            mainTaskDTO.setTags(taskBreakdown.getMainTask());
            mainTaskDTO.setReleaseVersionId(releaseVersionId);
            mainTaskDTO.setCreateUserId(createUserId);
            mainTaskDTO.setDifficultyLevel(difficultyLevel);
            mainTaskDTO.setAssignedUserId(assignedUserId);
            mainTaskDTO.setAssignedDate(assignedDate);
            mainTaskDTO.setStartDate(startDate);
            mainTaskDTO.setDeadline(deadline);
            mainTaskDTO.setCompletedDate(completedDate);

            // Create the main task
            TaskDTO createdMainTask = createTask(mainTaskDTO);

            // Create subtasks
            List<SubTaskDTO> subTasks = new ArrayList<>();
            for (TaskDetail taskDetail : taskBreakdown.getTasks()) {
                for (SubTaskDetail subTaskDetail : taskDetail.getSubtasks()) {
                    SubTaskDTO subTaskDTO = new SubTaskDTO();
                    subTaskDTO.setName(subTaskDetail.getSubtaskName());
                    subTaskDTO.setStatus(TaskStatus.TODO);
                    subTaskDTO.setTags(subTaskDetail.getTag());
                    subTaskDTO.setTaskId(createdMainTask.getId());

                    // Set deadline based on estimated hours
                    LocalDate subTaskDeadline = LocalDate.now().plusDays(
                            (long) Math.ceil(subTaskDetail.getEstimatedHours() / 8.0)
                    );
                    subTaskDTO.setDeadline(subTaskDeadline);

                    subTasks.add(subTaskDTO);
                }
            }

            // Update main task with subtasks
            createdMainTask.setSubTaskList(subTasks);
            TaskDTO updatedTask = updateTask(createdMainTask.getId(), createdMainTask);
            createdTasks.add(updatedTask);
        }

        return createdTasks;
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public TaskDTO assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setAssignedUser(user);
        task.setAssignedDate(LocalDate.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    public TaskDTO convertToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        BeanUtils.copyProperties(task, taskDTO);
        taskDTO.setReleaseVersionId(task.getReleaseVersion().getId());
        if (task.getAssignedUser() != null && task.getAssignedUser().getId() != null){
            taskDTO.setAssignedUserId(task.getAssignedUser().getId());
        }
        if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
            List<SubTaskDTO> subTaskDTOs = task.getSubTasks().stream()
                    .map(this::convertSubTaskToDTO)
                    .collect(Collectors.toList());
            taskDTO.setSubTaskList(subTaskDTOs);
        }
        return taskDTO;
    }

    public SubTaskDTO convertSubTaskToDTO(SubTask subTask) {
        SubTaskDTO subTaskDTO = new SubTaskDTO();
        BeanUtils.copyProperties(subTask, subTaskDTO);
        subTaskDTO.setTaskId(subTask.getTask().getId());
        if (subTask.getAssignedUser() != null) {
            subTaskDTO.setAssignedUserId(String.valueOf(subTask.getAssignedUser().getId()));
        }
        return subTaskDTO;
    }

    private TaskDTONew convertToDTONew(Task task) {
        TaskDTONew dto = new TaskDTONew();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setRoleCategory(task.getRoleCategory());
        dto.setStatus(task.getStatus());
        dto.setTags(task.getTags());
        dto.setAssignedDate(task.getAssignedDate());
        dto.setStartDate(task.getStartDate());
        dto.setDeadline(task.getDeadline());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setDifficultyLevel(task.getDifficultyLevel());
        dto.setPriorityLevel(task.getPriorityLevel());

        if (task.getAssignedUser() != null) {
            dto.setAssignedUser(convertToUserBasicDTO(task.getAssignedUser()));
        }

        dto.setSubTasks(subTaskService.getSubTasksByTaskId(task.getId()));
        return dto;
    }

    private UserBasicDTO convertToUserBasicDTO(User user) {
        UserBasicDTO dto = new UserBasicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

}
