package com.project_management.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SubTaskDTO {
    private Long id;
    private Long taskId;
    private String name;
    private Long createUserId;
    private Long assignedUserId;
    private String status;
    private String tags;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
}
