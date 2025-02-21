package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EffortResponse {
    private int total_effort;
    private Map<String, Integer> tasks;
    private Map<String, Map<String, Double>> role_distribution;
    private Map<String, Integer> max_story_points;
    private int avg_hours_per_story_point;

    public void setTotal_effort(int total_effort) {
        this.total_effort = total_effort;
    }

    public void setTasks(Map<String, Integer> tasks) {
        this.tasks = tasks;
    }

    public void setRole_distribution(Map<String, Map<String, Double>> role_distribution) {
        this.role_distribution = role_distribution;
    }

    public void setMax_story_points(Map<String, Integer> max_story_points) {
        this.max_story_points = max_story_points;
    }

    public void setAvg_hours_per_story_point(int avg_hours_per_story_point) {
        this.avg_hours_per_story_point = avg_hours_per_story_point;
    }
}
