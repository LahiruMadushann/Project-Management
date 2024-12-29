package com.project_management.controllers;

import com.project_management.dto.ClientDTO;
import com.project_management.dto.ClientSignUpResponse;
import com.project_management.dto.SignupRequestDTO;
import com.project_management.dto.SignupResponseDTO;
import com.project_management.services.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

//    @PostMapping("/client-signup")
//    public ResponseEntity<?> clientSignup(@RequestBody ClientDTO clientDTO) {
//        try {
//            ClientSignUpResponse signupResponse = signupService.signup(clientDTO);
//            return ResponseEntity.ok(signupResponse);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (Exception e) {
//            return handleException(e);
//        }
//    }

    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}