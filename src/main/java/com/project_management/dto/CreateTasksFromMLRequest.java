package com.project_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTasksFromMLRequest {
    private MLAnalysisResponse results;
    private Long releaseVersionId;
    private Long createUserId;
    private Integer difficultyLevel;
    private Long assignedUserId;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
}
