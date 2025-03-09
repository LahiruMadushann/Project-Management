package com.project_management.dto;

import lombok.Data;

@Data
public class ProjectStatusResponseDto {
    private String status;
    private boolean editable = true;
}
