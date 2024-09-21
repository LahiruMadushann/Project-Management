package com.project_management.dto;

import lombok.Data;

@Data
public class SignupResponseDTO {
    private String username;
    private String role;
    private String message;

    public SignupResponseDTO(String username, String role, String message) {
        this.username = username;
        this.role = role;
        this.message = message;
    }
}
