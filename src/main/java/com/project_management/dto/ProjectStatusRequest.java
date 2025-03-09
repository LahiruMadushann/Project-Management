package com.project_management.dto;

import lombok.Data;

@Data
public class ProjectStatusRequest {
    private Long projectId;
    private String status;
    private String comments;
}

