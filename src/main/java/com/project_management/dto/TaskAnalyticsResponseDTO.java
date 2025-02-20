package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskAnalyticsResponseDTO {
    @JsonProperty("tasks")
    private Map<String, List<TaskCountByPriorityDTO>> tasks;

    @JsonProperty("effort")
    private Integer effort;

    @JsonProperty("roles")
    private List<String> roles;

    public TaskAnalyticsResponseDTO() {
        this.tasks = new HashMap<>();
        this.roles = new ArrayList<>();
    }

    public Map<String, List<TaskCountByPriorityDTO>> getTasks() {
        return tasks;
    }

    public void setTasks(Map<String, List<TaskCountByPriorityDTO>> tasks) {
        this.tasks = tasks;
    }

    public Integer getEffort() {
        return effort;
    }

    public void setEffort(Integer effort) {
        this.effort = effort;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addTaskCountsForRole(String role, List<TaskCountByPriorityDTO> counts) {
        this.tasks.put(role, counts);
    }

    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(role);
    }

    public void setRolesList(List<String> roles) {
        this.roles = new ArrayList<>(roles);
    }
}
