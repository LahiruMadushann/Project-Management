package com.project_management.controllers;

import com.project_management.dto.SubTaskDTO;
import com.project_management.services.SubTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sub/tasks")
public class SubTaskController {

    @Autowired
    private SubTaskService subTaskService;

    @PostMapping
    public ResponseEntity<?> createSubTask(@RequestBody SubTaskDTO subTaskDTO) {
        try {
            SubTaskDTO createdSubTask = subTaskService.createSubTask(subTaskDTO);
            return new ResponseEntity<>(createdSubTask, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating subtask: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubTaskById(@PathVariable Long id) {
        try {
            SubTaskDTO subTask = subTaskService.getSubTaskById(id);
            return ResponseEntity.ok(subTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving subtask with ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSubTasks() {
        try {
            List<SubTaskDTO> subTasks = subTaskService.getAllSubTasks();
            return ResponseEntity.ok(subTasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all subtasks: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubTask(@PathVariable Long id, @RequestBody SubTaskDTO subTaskDTO) {
        try {
            SubTaskDTO updatedSubTask = subTaskService.updateSubTask(id, subTaskDTO);
            return ResponseEntity.ok(updatedSubTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating subtask with ID " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubTask(@PathVariable Long id) {
        try {
            subTaskService.deleteSubTask(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting subtask with ID " + id + ": " + e.getMessage());
        }
    }

    @PatchMapping("/{subTaskId}/assign/{userId}")
    public ResponseEntity<?> assignSubTaskToUser(@PathVariable Long subTaskId, @PathVariable Long userId) {
        try {
            SubTaskDTO updatedSubTask = subTaskService.assignSubTaskToUser(subTaskId, userId);
            return ResponseEntity.ok(updatedSubTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error assigning subtask " + subTaskId + " to user " + userId + ": " + e.getMessage());
        }
    }
}