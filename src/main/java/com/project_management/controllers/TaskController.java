package com.project_management.controllers;

import com.project_management.dto.*;
import com.project_management.services.ReleaseVersionService;
import com.project_management.services.TaskService;
import com.project_management.servicesImpl.TaskCreationFromMLService;
import com.project_management.utills.MLServiceConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskCreationFromMLService taskCreationFromMLService;

    @Value("${ml.service.url:http://127.0.0.1:5000/analyze}")
    private String mlServiceUrl;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO createdTask = taskService.createTask(taskDTO);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating task: ", e);
            return ResponseEntity.badRequest().body("Error creating task: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            log.error("Error retrieving task with ID {}: ", id, e);
            return ResponseEntity.badRequest()
                    .body("Error retrieving task with ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error retrieving all tasks: ", e);
            return ResponseEntity.badRequest().body("Error retrieving all tasks: " + e.getMessage());
        }
    }

    @GetMapping("/release-version/{releaseVersionId}")
    public ResponseEntity<?> getTasksByReleaseVersionId(@PathVariable Long releaseVersionId) {
        try {
            List<TaskDTO> tasks = taskService.getTasksByReleaseVersionId(releaseVersionId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error retrieving tasks for release version {}: ", releaseVersionId, e);
            return ResponseEntity.badRequest()
                    .body("Error retrieving tasks for release version " + releaseVersionId + ": " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("Error updating task with ID {}: ", id, e);
            return ResponseEntity.badRequest()
                    .body("Error updating task with ID " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting task with ID {}: ", id, e);
            return ResponseEntity.badRequest()
                    .body("Error deleting task with ID " + id + ": " + e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId) {
        try {
            TaskDTO updatedTask = taskService.assignTaskToUser(taskId, userId);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("Error assigning task {} to user {}: ", taskId, userId, e);
            return ResponseEntity.badRequest()
                    .body("Error assigning task " + taskId + " to user " + userId + ": " + e.getMessage());
        }
    }

    @PostMapping("/create-from-stories")
    public ResponseEntity<?> createTasksFromUserStories(@RequestBody CreateTasksFromMLRequest request) {
        try {
            if (request.getResults() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("MLAnalysisResponse is required"));
            }

            List<TaskDTO> createdTasks = taskCreationFromMLService.createTasksFromMLAnalysis(
                    request.getResults(),
                    request.getReleaseVersionId(),
                    request.getCreateUserId(),
                    request.getDifficultyLevel(),
                    request.getAssignedUserId(),
                    request.getAssignedDate(),
                    request.getStartDate(),
                    request.getDeadline(),
                    request.getCompletedDate()
            );
            log.info("Successfully created {} tasks from ML analysis", createdTasks.size());
            return new ResponseEntity<>(createdTasks, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error processing request: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error processing request: " + e.getMessage()));
        }
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