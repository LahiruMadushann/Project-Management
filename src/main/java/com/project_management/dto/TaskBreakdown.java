package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskBreakdown {
    private double estimatedHours;
    private String mainTask;
    private List<TaskDetail> tasks;
}
