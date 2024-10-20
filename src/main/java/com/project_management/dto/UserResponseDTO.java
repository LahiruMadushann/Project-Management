package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String roleName;
    private List<String> permissions;
}
