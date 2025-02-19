package com.project_management.dto;

import com.project_management.models.enums.RoleCategory;
import com.project_management.models.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubTaskDTONew {
    private Long id;
    private String name;
    private RoleCategory roleCategory;
    private TaskStatus status;
    private String tags;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
    private UserBasicDTO assignedUser;
}
