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
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        SignupResponseDTO signupResponse = signupService.signup(signupRequestDTO);
        return ResponseEntity.ok(signupResponse);
    }
}
