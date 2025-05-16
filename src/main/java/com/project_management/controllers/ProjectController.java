package com.project_management.controllers;

import com.project_management.dto.*;
import com.project_management.models.AdvanceDetails;
import com.project_management.models.ProjectBudget;
import com.project_management.security.utils.SecurityUtil;
import com.project_management.services.ProjectService;
import com.project_management.services.ReleaseVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            projectDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            ProjectDTO createdProject = projectService.createProject(projectDTO);
            return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        try {
            ProjectDTO project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        try {
            List<ProjectDTO> projects = projectService.getAllProjects();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/res/b_active")
    public ResponseEntity<?> getAllProjectsBudgetActive() {
        try {
            List<ProjectDTO> projects = projectService.getAllProjectsBudgetActive();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        try {
            projectDTO.setId(SecurityUtil.getCurrentUserId());
            ProjectDTO updatedProject = projectService.updateProject(id, projectDTO);
            return ResponseEntity.ok(updatedProject);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateProjectStatus(@RequestBody ProjectStatusRequest request) {
        try {
            ProjectStatusResponseDto updatedProject = projectService.updateProjectStatus(request);
            return ResponseEntity.ok(updatedProject);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }
    @GetMapping("/user/{userId}/assigned")
    public ResponseEntity<List<ProjectDetailsDTO>> getAssignedProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getProjectsAssignedToUser(userId));
    }

    @PostMapping("/advance")
    public ResponseEntity<EffortCombinedCallResponse> projectAdvance(@RequestBody AdvanceDetailsDTO advanceDetailsDTO){
        return ResponseEntity.ok().body(projectService.saveAdvance(advanceDetailsDTO));
    }

    @GetMapping("/advance/all/{id}")
    public ResponseEntity<List<AdvanceDetails>> getAdvance(@PathVariable Long id){
        return ResponseEntity.ok().body(projectService.getAdvance(id));
    }

    @GetMapping("/advance/{projectId}")
    public ResponseEntity<AdvanceDetails> getAdvanceDetails(@PathVariable Long projectId) {
        AdvanceDetails advanceDetails = projectService.getAdvanceDetailsByProjectId(projectId);
        return ResponseEntity.ok().body(advanceDetails);
    }

    @PostMapping("/advance/save/budget")
    public ResponseEntity<ProjectBudget> saveBudget1(@RequestBody ProjectBudgetGraphDto requestDto){
        System.out.println("here");
        return ResponseEntity.ok().body(projectService.saveBudget(requestDto));
    }

    @GetMapping("/get/stories")
    public ResponseEntity<?> getStories() {
        try {
            return ResponseEntity.ok(releaseVersionService.getUserStoriesByProjectId());
        } catch (Exception e) {
            return handleException(e);
        }
    }


    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}