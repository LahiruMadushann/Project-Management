package com.project_management.dto;

import com.project_management.models.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private Long releaseVersionId;
    private String name;
    private Long createUserId;
    private Long assignedUserId;
    private TaskStatus status;
    private String tags;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
    private List<SubTaskDTO> subTaskList;
}
