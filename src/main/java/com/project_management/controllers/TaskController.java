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
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> task = taskService.getAllTasks();
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<TaskDTO> assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId) {
        TaskDTO updatedTask = taskService.assignTaskToUser(taskId, userId);
        return ResponseEntity.ok(updatedTask);
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
