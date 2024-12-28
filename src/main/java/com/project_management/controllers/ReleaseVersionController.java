package com.project_management.controllers;

import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.services.ReleaseVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
public class ReleaseVersionController {

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @PostMapping
    public ResponseEntity<?> createReleaseVersion(@RequestBody ReleaseVersionDTO releaseVersionDTO) {
        try {
            ReleaseVersionDTO createdReleaseVersion = releaseVersionService.createReleaseVersion(releaseVersionDTO);
            return new ResponseEntity<>(createdReleaseVersion, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating release version: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReleaseVersionById(@PathVariable Long id) {
        try {
            ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(id);
            return ResponseEntity.ok(releaseVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving release version with ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getReleaseVersionsByProjectId(@PathVariable Long projectId) {
        try {
            List<ReleaseVersionDTO> releaseVersions = releaseVersionService.getReleaseVersionsByProjectId(projectId);
            return ResponseEntity.ok(releaseVersions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving release versions for project ID " + projectId + ": " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllReleaseVersions() {
        try {
            List<ReleaseVersionDTO> releaseVersions = releaseVersionService.getAllReleaseVersions();
            return ResponseEntity.ok(releaseVersions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all release versions: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReleaseVersion(@PathVariable Long id, @RequestBody ReleaseVersionDTO releaseVersionDTO) {
        try {
            ReleaseVersionDTO updatedReleaseVersion = releaseVersionService.updateReleaseVersion(id, releaseVersionDTO);
            return ResponseEntity.ok(updatedReleaseVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating release version with ID " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReleaseVersion(@PathVariable Long id) {
        try {
            releaseVersionService.deleteReleaseVersion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting release version with ID " + id + ": " + e.getMessage());
        }
    }
}