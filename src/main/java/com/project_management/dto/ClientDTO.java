package com.project_management.dto;

import lombok.Data;

@Data
public class ClientDTO {
    private Long clientId;
    private String clientName;
    private String email;
    private String password;
    private String createdByUsername;
    private boolean isActive;
}

