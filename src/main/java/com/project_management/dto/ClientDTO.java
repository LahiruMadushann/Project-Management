package com.project_management.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ClientDTO {
    private String username;
    private String password;
    private String email;
    private String roleName = "ROLE_CLIENT";
    private Long clientId;
    private Long projectId;
    private Integer userId;
}

