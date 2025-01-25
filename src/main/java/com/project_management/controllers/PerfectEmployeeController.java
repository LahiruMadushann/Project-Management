package com.project_management.controllers;

import com.project_management.dto.PerfectEmployeeDTO;
import com.project_management.models.PerfectEmployee;
import com.project_management.models.PerfectRole;
import com.project_management.models.enums.RoleCategory;
import com.project_management.security.jwt.JwtTokenProvider;
import com.project_management.services.PerfectEmployeeService;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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
    public ResponseEntity<?> createPerfectEmployee(@RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        try {
            String token = getTokenFromRequest();
            Long userId = jwtTokenProvider.getUserId(token);
            PerfectEmployeeDTO createdEmployee = perfectEmployeeService.savePerfectEmployee(perfectEmployeeDTO, userId);
            return ResponseEntity.ok(createdEmployee);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updatePerfectEmployee(@PathVariable String employeeId, @RequestBody PerfectEmployeeDTO perfectEmployeeDTO) {
        try {
            PerfectEmployeeDTO updatedEmployee = perfectEmployeeService.updatePerfectEmployee(employeeId, perfectEmployeeDTO);
            return ResponseEntity.ok(updatedEmployee);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<?> deletePerfectEmployee(@PathVariable String employeeId) {
        try {
            perfectEmployeeService.deletePerfectEmployee(employeeId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getPerfectEmployee(@PathVariable String employeeId) {
        try {
            PerfectEmployeeDTO perfectEmployee = perfectEmployeeService.getPerfectEmployee(employeeId);
            return ResponseEntity.ok(perfectEmployee);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPerfectEmployees() {
        try {
            List<PerfectEmployeeDTO> perfectEmployees = perfectEmployeeService.getAllPerfectEmployees();
            return ResponseEntity.ok(perfectEmployees);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<String> roles = perfectRoleService.getAllRoles().stream().map(PerfectRole::getRoleName).toList();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/category/{roleCategory}/roles")
    public ResponseEntity<?> getRolesByCategory(@PathVariable RoleCategory roleCategory) {
        try {
            List<String> roles = perfectRoleService.getAllRolesCategory(roleCategory)
                    .stream()
                    .map(PerfectEmployee::getRoleName)
                    .distinct()
                    .collect(Collectors.toList());
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return handleException(e);
        }
    }
    private String getTokenFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        throw new RuntimeException("No authentication token found");
    }

    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}