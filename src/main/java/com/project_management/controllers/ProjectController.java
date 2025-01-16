package com.project_management.controllers;

import com.project_management.dto.ProjectDTO;
import com.project_management.security.utils.SecurityUtil;
import com.project_management.services.ProjectService;
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

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO) {
        try {
            projectDTO.setId(SecurityUtil.getCurrentUserId());
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

    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}