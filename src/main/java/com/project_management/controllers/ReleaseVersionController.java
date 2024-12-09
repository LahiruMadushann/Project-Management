package com.project_management.controllers;

import com.project_management.dto.ProjectDTO;
import com.project_management.dto.ReleaseVersionDTO;
import com.project_management.services.ProjectService;
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
    public ResponseEntity<ReleaseVersionDTO> createReleaseVersion(@RequestBody ReleaseVersionDTO releaseVersionDTO) {
        ReleaseVersionDTO createdReleaseVersion = releaseVersionService.createReleaseVersion(releaseVersionDTO);
        return new ResponseEntity<>(createdReleaseVersion, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReleaseVersionDTO> getReleaseVersionById(@PathVariable Long id) {
        ReleaseVersionDTO releaseVersion = releaseVersionService.getReleaseVersionById(id);
        return ResponseEntity.ok(releaseVersion);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ReleaseVersionDTO>> getReleaseVersionsByProjectId(@PathVariable Long projectId) {
        List<ReleaseVersionDTO> releaseVersions = releaseVersionService.getReleaseVersionsByProjectId(projectId);
        return ResponseEntity.ok(releaseVersions);
    }

    @GetMapping
    public ResponseEntity<List<ReleaseVersionDTO>> getAllReleaseVersions() {
        List<ReleaseVersionDTO> releaseVersion = releaseVersionService.getAllReleaseVersions();
        return ResponseEntity.ok(releaseVersion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReleaseVersionDTO> updateReleaseVersion(@PathVariable Long id, @RequestBody ReleaseVersionDTO releaseVersionDTO) {
        ReleaseVersionDTO updatedReleaseVersion = releaseVersionService.updateReleaseVersion(id, releaseVersionDTO);
        return ResponseEntity.ok(updatedReleaseVersion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReleaseVersion(@PathVariable Long id) {
        releaseVersionService.deleteReleaseVersion(id);
        return ResponseEntity.noContent().build();
    }
}
