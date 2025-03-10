package com.project_management.dto;

import com.project_management.models.enums.RoleCategory;
import com.project_management.models.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SubTaskDTO {
    private Long id;
    private Long taskId;
    private String name;
    private Long createUserId;
    private String assignedUserId;
    private TaskStatus status;
    private String tags;
    private RoleCategory roleCategory;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
}
