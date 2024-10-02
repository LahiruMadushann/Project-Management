package com.project_management.controllers;

import com.project_management.dto.SubTaskDTO;
import com.project_management.dto.TaskDTO;
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
    public ResponseEntity<SubTaskDTO> createSubTask(@RequestBody SubTaskDTO subTaskDTO) {
        SubTaskDTO createdSubTask = subTaskService.createSubTask(subTaskDTO);
        return new ResponseEntity<>(createdSubTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubTaskDTO> getSubTaskById(@PathVariable Long id) {
        SubTaskDTO subTask = subTaskService.getSubTaskById(id);
        return ResponseEntity.ok(subTask);
    }

    @GetMapping
    public ResponseEntity<List<SubTaskDTO>> getAllSubTasks() {
        List<SubTaskDTO> subTask = subTaskService.getAllSubTasks();
        return ResponseEntity.ok(subTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubTaskDTO> updateSubTask(@PathVariable Long id, @RequestBody SubTaskDTO subTaskDTO) {
        SubTaskDTO updatedSubTask = subTaskService.updateSubTask(id, subTaskDTO);
        return ResponseEntity.ok(updatedSubTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubTask(@PathVariable Long id) {
        subTaskService.deleteSubTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{subTaskId}/assign/{userId}")
    public ResponseEntity<SubTaskDTO> assignSubTaskToUser(@PathVariable Long subTaskId, @PathVariable Long userId) {
        SubTaskDTO updatedSubTask = subTaskService.assignSubTaskToUser(subTaskId, userId);
        return ResponseEntity.ok(updatedSubTask);
    }

}
