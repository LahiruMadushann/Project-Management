package com.project_management.dto;

import lombok.Data;

@Data
public class PasswordResetDto {
    private String username;
    private String password;
    private String newPassword;
}
