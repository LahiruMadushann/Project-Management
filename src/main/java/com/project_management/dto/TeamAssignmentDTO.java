package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project_management.models.TeamAssignmentId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamAssignmentDTO {
    private TeamAssignmentId id;

    private String employeeName;

    private String roleName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private Double exhaustedPercentage;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
