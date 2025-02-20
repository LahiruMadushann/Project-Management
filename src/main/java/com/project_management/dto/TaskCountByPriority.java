package com.project_management.dto;

import lombok.Data;

@Data
public class TaskCountByPriority {
    private long count;
    private String priority;
}