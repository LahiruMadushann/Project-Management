package com.project_management.controllers;

import com.project_management.dto.CriticalPathResponse;
import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.security.utils.SecurityUtil;
import com.project_management.services.ReleaseVersionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/versions")
public class ReleaseVersionController {

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @PostMapping
    public ResponseEntity<?> createReleaseVersion(@RequestBody ReleaseVersionDTO releaseVersionDTO) {
        try {
            releaseVersionDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            ReleaseVersionDTO createdReleaseVersion = releaseVersionService.createReleaseVersion(releaseVersionDTO);
            return new ResponseEntity<>(createdReleaseVersion, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReleaseVersionById(@PathVariable Long id) {
        try {
            ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(id);
            return ResponseEntity.ok(releaseVersion);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getReleaseVersionsByProjectId(@PathVariable Long projectId) {
        try {
            List<ReleaseVersionDTO> releaseVersions = releaseVersionService.getReleaseVersionsByProjectId(projectId);
            return ResponseEntity.ok(releaseVersions);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }


    @GetMapping("/critical/{id}")
    public ResponseEntity<?> getTaskByProjectId(@PathVariable Long id) {
        try {
            CriticalPathResponse response = releaseVersionService.criticalPath(id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllReleaseVersions() {
        try {
            List<ReleaseVersionDTO> releaseVersions = releaseVersionService.getAllReleaseVersions();
            return ResponseEntity.ok(releaseVersions);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReleaseVersion(@PathVariable Long id, @RequestBody ReleaseVersionDTO releaseVersionDTO) {
        try {
            releaseVersionDTO.setCreateUserId(SecurityUtil.getCurrentUserId());
            ReleaseVersionDTO updatedReleaseVersion = releaseVersionService.updateReleaseVersion(id, releaseVersionDTO);
            return ResponseEntity.ok(updatedReleaseVersion);
        } catch (NoSuchElementException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReleaseVersion(@PathVariable Long id) {
        try {
            releaseVersionService.deleteReleaseVersion(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException | EntityNotFoundException e) {
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