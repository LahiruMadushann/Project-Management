package com.project_management.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TasksByRole {
    private Map<String, List<TaskCountByPriority>> tasks;
    private int effort;
    private List<String> roles;

}
