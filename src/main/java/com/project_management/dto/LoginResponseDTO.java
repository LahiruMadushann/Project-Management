package com.project_management.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginResponseDTO {
    private String token;
    private String role;
    private List<String> permissions;

    public LoginResponseDTO(String token, String role, List<String> permissions) {
        this.token = token;
        this.role = role;
        this.permissions = permissions;
    }
}
