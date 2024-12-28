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
    public ResponseEntity<?> createPerfectEmployee(@RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        try {
            String token = getTokenFromRequest();
            Long userId = jwtTokenProvider.getUserId(token);
            PerfectEmployeeDTO createdEmployee = perfectEmployeeService.savePerfectEmployee(perfectEmployeeDTO, userId);
            return ResponseEntity.ok(createdEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating perfect employee: " + e.getMessage());
        }
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updatePerfectEmployee(@PathVariable String employeeId,
                                                   @RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        try {
            PerfectEmployeeDTO updatedEmployee = perfectEmployeeService.updatePerfectEmployee(employeeId, perfectEmployeeDTO);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating perfect employee: " + e.getMessage());
        }
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<?> deletePerfectEmployee(@PathVariable String employeeId) {
        try {
            perfectEmployeeService.deletePerfectEmployee(employeeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting perfect employee: " + e.getMessage());
        }
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getPerfectEmployee(@PathVariable String employeeId) {
        try {
            PerfectEmployeeDTO employee = perfectEmployeeService.getPerfectEmployee(employeeId);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving perfect employee: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPerfectEmployees() {
        try {
            List<PerfectEmployeeDTO> employees = perfectEmployeeService.getAllPerfectEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all perfect employees: " + e.getMessage());
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<String> roles = perfectRoleService.getAllRoles().stream()
                    .map(PerfectRole::getRoleName)
                    .toList();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving all roles: " + e.getMessage());
        }
    }

    private String getTokenFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        throw new RuntimeException("No authentication token found");
    }
}