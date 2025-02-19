package com.project_management.dto;

import com.project_management.models.enums.PriorityLevel;
import com.project_management.models.enums.RoleCategory;
import com.project_management.models.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskDTONew {
    private Long id;
    private String name;
    private RoleCategory roleCategory;
    private TaskStatus status;
    private String tags;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
    private Integer difficultyLevel;
    private PriorityLevel priorityLevel;
    private UserBasicDTO assignedUser;
    private List<SubTaskDTONew> subTasks;
}

