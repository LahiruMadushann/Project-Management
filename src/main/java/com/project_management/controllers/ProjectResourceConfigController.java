package com.project_management.controllers;

import com.project_management.models.ProjectResourceConfig;
import com.project_management.services.ProjectResourceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/r-config")
public class ProjectResourceConfigController {

    @Autowired
    private ProjectResourceConfigService projectResourceConfigService;

    @GetMapping("/{projectId}")
    private ResponseEntity<ProjectResourceConfig> getConfigList(
            @PathVariable Long projectId){
        return ResponseEntity.ok().body(projectResourceConfigService.getConfigByProject(projectId));
    }
}
