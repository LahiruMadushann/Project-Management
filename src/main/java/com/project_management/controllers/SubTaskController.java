package com.project_management.controllers;

import com.project_management.dto.SubTaskDTO;
import com.project_management.security.utils.SecurityUtil;
import com.project_management.services.SubTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/sub/tasks")
public class SubTaskController {

    @Autowired
    private SubTaskService subTaskService;

    @PostMapping
    public ResponseEntity<?> createSubTask(@RequestBody SubTaskDTO subTaskDTO) {
        try {
            subTaskDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            SubTaskDTO createdSubTask = subTaskService.createSubTask(subTaskDTO);
            return new ResponseEntity<>(createdSubTask, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubTaskById(@PathVariable Long id) {
        try {
            SubTaskDTO subTask = subTaskService.getSubTaskById(id);
            return ResponseEntity.ok(subTask);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSubTasks() {
        try {
            List<SubTaskDTO> subTasks = subTaskService.getAllSubTasks();
            return ResponseEntity.ok(subTasks);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubTask(@PathVariable Long id, @RequestBody SubTaskDTO subTaskDTO) {
        try {
            subTaskDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            SubTaskDTO updatedSubTask = subTaskService.updateSubTask(id, subTaskDTO);
            return ResponseEntity.ok(updatedSubTask);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubTask(@PathVariable Long id) {
        try {
            subTaskService.deleteSubTask(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PatchMapping("/{subTaskId}/assign/{userId}")
    public ResponseEntity<?> assignSubTaskToUser(@PathVariable Long subTaskId, @PathVariable Long userId) {
        try {
            SubTaskDTO updatedSubTask = subTaskService.assignSubTaskToUser(subTaskId, userId);
            return ResponseEntity.ok(updatedSubTask);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}