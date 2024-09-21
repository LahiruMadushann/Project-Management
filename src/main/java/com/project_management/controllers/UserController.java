package com.project_management.controllers;

import com.project_management.servicesImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/manage")
    public ResponseEntity<String> manageUsers() {
        try {
            userService.adminPermission();
            return ResponseEntity.ok("Users management access granted");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("You don't have permission to manage users.");
        }
    }


    @GetMapping("/view")
    public ResponseEntity<String> viewUsers() {
        try {
            userService.managerPermission();
            return ResponseEntity.ok("Users view access granted");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("You don't have permission to view users.");
        }
    }

    @GetMapping("/delete")
    public ResponseEntity<String> deleteUsers() {
        try {
            userService.employeePermission();
            return ResponseEntity.ok("Users deletion access granted");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("You don't have permission to delete users.");
        }
    }

}
