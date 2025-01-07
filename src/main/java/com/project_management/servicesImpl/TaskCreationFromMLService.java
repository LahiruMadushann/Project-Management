package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.ReleaseVersion;
import com.project_management.models.SubTask;
import com.project_management.models.Task;
import com.project_management.models.User;
import com.project_management.models.enums.TaskStatus;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.repositories.SubTaskRepository;
import com.project_management.repositories.TaskRepository;
import com.project_management.repositories.UserRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskCreationFromMLService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

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
            for (TaskDetail taskDetail : taskBreakdown.getTasks()) {
                // Create main task
                Task mainTask = new Task();
                mainTask.setName(taskDetail.getTaskName());
                mainTask.setStatus(TaskStatus.TODO);
                mainTask.setTags(taskBreakdown.getMainTask());
                mainTask.setReleaseVersion(releaseVersion);
                mainTask.setCreateUserId(createUser.getId());
                mainTask.setDifficultyLevel(difficultyLevel);
                mainTask.setAssignedUser(assignedUser);
                mainTask.setAssignedDate(assignedDate);
                mainTask.setStartDate(startDate);
                mainTask.setDeadline(deadline);
                mainTask.setCompletedDate(completedDate);
                mainTask.setCreatedAt(LocalDateTime.now());
                mainTask.setUpdatedAt(LocalDateTime.now());

                // Save the main task first
                Task savedMainTask = taskRepository.save(mainTask);

                // Create and save subtasks
                List<SubTask> subtasks = new ArrayList<>();
                for (SubTaskDetail subTaskDetail : taskDetail.getSubtasks()) {
                    SubTask subTask = new SubTask();
                    subTask.setName(subTaskDetail.getSubtaskName());
                    subTask.setStatus(TaskStatus.TODO);
                    subTask.setTags(subTaskDetail.getTag());
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
                }

                // Save all subtasks
                List<SubTask> savedSubtasks = subTaskRepository.saveAll(subtasks);
                savedMainTask.setSubTasks(savedSubtasks);

                // Save the updated main task with subtask references
                Task finalSavedTask = taskRepository.save(savedMainTask);

                // Convert to DTO and add to result list
                createdTasks.add(convertTaskToDTO(finalSavedTask));
            }
        }

        return createdTasks;
    }

    @Transactional
    public List<TaskDTO> createTasksFromStories(CreateTasksFromStoriesRequest request) {
        try {
            // Create request body exactly matching Python API expectations
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
                    request.getAssignedUserId(),
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
        subTaskDTO.setCreateUserId(subTask.getCreateUserId());
        if (subTask.getAssignedUser() != null) {
            subTaskDTO.setAssignedUserId(subTask.getAssignedUser().getId());
        }
        subTaskDTO.setAssignedDate(subTask.getAssignedDate());
        subTaskDTO.setStartDate(subTask.getStartDate());
        subTaskDTO.setDeadline(subTask.getDeadline());
        subTaskDTO.setCompletedDate(subTask.getCompletedDate());
        return subTaskDTO;
    }
}