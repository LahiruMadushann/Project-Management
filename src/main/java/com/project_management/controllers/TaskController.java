package com.project_management.controllers;

import com.project_management.dto.*;
import com.project_management.models.Project;
import com.project_management.models.ReleaseVersion;
import com.project_management.models.Task;
import com.project_management.models.enums.ProjectStatus;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ReleaseVersionRepository;
import com.project_management.security.utils.SecurityUtil;
import com.project_management.services.TaskService;
import com.project_management.servicesImpl.TaskCreationFromMLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskCreationFromMLService taskCreationFromMLService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Value("${ml.service.url:http://127.0.0.1:5000/analyze}")
    private String mlServiceUrl;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            taskDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            TaskDTO createdTask = taskService.createTask(taskDTO);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<?> getTaskByProjectId(@PathVariable Long id) {
        try {
            List<TaskDTO> task = taskService.getTaskByProjectId(id);
            return ResponseEntity.ok(task);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PatchMapping("/auto-assign")
    public ResponseEntity<?> addAssignUsers(@RequestParam Integer projectId) {
        try {
           List<TaskDTO> tasks = taskCreationFromMLService.assignAutoUsers(Long.valueOf(projectId));
           return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/release-version/{releaseVersionId}")
    public ResponseEntity<?> getTasksByReleaseVersionId(@PathVariable Long releaseVersionId) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByReleaseVersionId(releaseVersionId);
            return ResponseEntity.ok(tasks);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        try {
            taskDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            return ResponseEntity.ok(updatedTask);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId) {
        try {
            TaskDTO updatedTask = taskService.assignTaskToUser(taskId, userId);
            return ResponseEntity.ok(updatedTask);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

//    @PostMapping("/create-from-stories")
//    public ResponseEntity<?> createTasksFromUserStories(@RequestBody CreateTasksFromMLRequest request) {
//        try {
//            if (request.getResults() == null) {
//                return ResponseEntity
//                        .status(HttpStatus.BAD_REQUEST)
//                        .body(new ErrorResponse("MLAnalysisResponse is required"));
//            }
//
//            List<TaskDTO> createdTasks = taskCreationFromMLService.createTasksFromMLAnalysis(
//                    request.getResults(),
//                    request.getReleaseVersionId(),
//                    request.getCreateUserId(),
//                    request.getDifficultyLevel(),
//                    request.getAssignedUserId(),
//                    request.getAssignedDate(),
//                    request.getStartDate(),
//                    request.getDeadline(),
//                    request.getCompletedDate()
//            );
//            log.info("Successfully created {} tasks from ML analysis", createdTasks.size());
//            return new ResponseEntity<>(createdTasks, HttpStatus.CREATED);
//        } catch (Exception e) {
//            log.error("Error processing request: ", e);
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Error processing request: " + e.getMessage()));
//        }
//    }

    @PostMapping("/create-from-stories")
    public ResponseEntity<?> createTasksFromUserStories(@RequestBody CreateTasksFromStoriesRequest request) {
        try {
            request.setCreateUserId(SecurityUtil.getCurrentUserId());
            if (request.getSimpleUserStories() == null || request.getSimpleUserStories().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("User stories are required"));
            }
            ReleaseVersion releaseVersion = releaseVersionRepository.findById(request.getReleaseVersionId()).orElseThrow();
            Project project = releaseVersion.getProject();
            if(project.getStatus() != ProjectStatus.ACCEPTED){
                return new ResponseEntity<>("", HttpStatus.LOCKED);
            }

            List<TaskDTO> createdTasks = taskCreationFromMLService.createTasksFromStories(request);
            log.info("Successfully created {} tasks from user stories", createdTasks.size());
            return new ResponseEntity<>(createdTasks, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing request: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing request: " + e.getMessage()));
        }
    }

    private ResponseEntity<String> handleException(Exception e) {
        log.error("Error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}