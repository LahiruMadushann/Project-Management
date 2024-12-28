package com.project_management.controllers;

import com.project_management.dto.SignupRequestDTO;
import com.project_management.dto.SignupResponseDTO;
import com.project_management.services.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    @Autowired
    private SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            SignupResponseDTO signupResponse = signupService.signup(signupRequestDTO);
            return ResponseEntity.ok(signupResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error during user signup: " + e.getMessage());
        }
    }
}
