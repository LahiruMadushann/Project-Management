package com.project_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientSignUpResponse {
    private String clientName;
    private String message;
}
