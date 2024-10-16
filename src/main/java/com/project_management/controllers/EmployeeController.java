package com.project_management.controllers;

import com.project_management.dto.EmployeeDTO;
import com.project_management.services.EmployeeService;
import com.project_management.servicesImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO, @RequestParam Long userId) {
        userService.managerPermission();
        return ResponseEntity.ok(employeeService.createEmployee(employeeDTO, userId));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String employeeId, @RequestBody EmployeeDTO employeeDTO) {
        userService.managerPermission();
        return ResponseEntity.ok(employeeService.updateEmployee(employeeId, employeeDTO));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        userService.managerPermission();
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable String employeeId) {
        userService.managerPermission();
        return ResponseEntity.ok(employeeService.getEmployee(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        userService.managerPermission();
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
}