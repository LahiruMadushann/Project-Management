package com.project_management.dto;

import lombok.Data;

@Data
public class SignupResponseDTO {
    private Integer userId;
    private String username;
    private String role;
    private String message;

    public SignupResponseDTO(Integer userId, String username, String role, String message) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = message;
    }
}
