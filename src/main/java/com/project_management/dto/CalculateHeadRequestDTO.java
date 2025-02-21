package com.project_management.dto;

import java.util.Map;

public class CalculateHeadRequestDTO {
    private double total_effort;
    private Map<String, Integer> tasks;
    private Map<String, Map<String, Double>> role_distribution;
    private Map<String, Double> max_story_points;
    private double avg_hours_per_story_point;

    // Getters and Setters
    public double getTotal_effort() {
        return total_effort;
    }

    public void setTotal_effort(double total_effort) {
        this.total_effort = total_effort;
    }

    public Map<String, Integer> getTasks() {
        return tasks;
    }

    public void setTasks(Map<String, Integer> tasks) {
        this.tasks = tasks;
    }

    public Map<String, Map<String, Double>> getRole_distribution() {
        return role_distribution;
    }

    public void setRole_distribution(Map<String, Map<String, Double>> role_distribution) {
        this.role_distribution = role_distribution;
    }

    public Map<String, Double> getMax_story_points() {
        return max_story_points;
    }

    public void setMax_story_points(Map<String, Double> max_story_points) {
        this.max_story_points = max_story_points;
    }

    public double getAvg_hours_per_story_point() {
        return avg_hours_per_story_point;
    }

    public void setAvg_hours_per_story_point(double avg_hours_per_story_point) {
        this.avg_hours_per_story_point = avg_hours_per_story_point;
    }
}
