package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EffortResponse {
    @JsonProperty("total_effort")
    private int totalEffort = 3000;

    @JsonProperty("tasks")
    private Map<String, Integer> tasks = new HashMap<>();

    @JsonProperty("role_distribution")
    private Map<String, Map<String, Double>> roleDistribution = new HashMap<>();

    @JsonProperty("max_story_points")
    private Map<String, Integer> maxStoryPoints = new HashMap<>();

    @JsonProperty("avg_hours_per_story_point")
    private int avgHoursPerStoryPoint = 1;
}
