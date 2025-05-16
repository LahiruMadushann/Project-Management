package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project_management.models.enums.PriorityLevel;
import lombok.Data;

import java.util.List;

@Data
public class TaskDetail {
    @JsonProperty("estimated_hours")
    private double estimatedHours;
    @JsonProperty("subtasks")
    private List<SubTaskDetail> subtasks;
    @JsonProperty("task_name")
    private String taskName;
    @JsonProperty("priority_level")
    private PriorityLevel priorityLevel;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
}