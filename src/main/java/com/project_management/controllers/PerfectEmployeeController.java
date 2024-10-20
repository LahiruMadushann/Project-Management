package com.project_management.controllers;

import com.project_management.dto.PerfectEmployeeDTO;
import com.project_management.models.PerfectRole;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.PerfectEmployeeService;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/perfect-employees")
public class PerfectEmployeeController {

    @Autowired
    private PerfectEmployeeService perfectEmployeeService;

    @Autowired
    private PerfectRoleService perfectRoleService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<PerfectEmployeeDTO> createPerfectEmployee(@RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        String token = getTokenFromRequest();
        Long userId = jwtTokenProvider.getUserId(token);
        return ResponseEntity.ok(perfectEmployeeService.savePerfectEmployee(perfectEmployeeDTO, userId));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<PerfectEmployeeDTO> updatePerfectEmployee(@PathVariable String employeeId, @RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        return ResponseEntity.ok(perfectEmployeeService.updatePerfectEmployee(employeeId, perfectEmployeeDTO));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deletePerfectEmployee(@PathVariable String employeeId) {
        perfectEmployeeService.deletePerfectEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<PerfectEmployeeDTO> getPerfectEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(perfectEmployeeService.getPerfectEmployee(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<PerfectEmployeeDTO>> getAllPerfectEmployees() {
        return ResponseEntity.ok(perfectEmployeeService.getAllPerfectEmployees());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(perfectRoleService.getAllRoles().stream().map(PerfectRole::getRoleName).toList());
    }

    private String getTokenFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        throw new RuntimeException("No authentication token found");
    }
}