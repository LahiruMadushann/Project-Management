package com.project_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EffortCalculationResponse {
    private int totalEffort;
    private Map<String, Integer> tasks;
    private Map<String, Map<String, Double>> roleDistribution;
    private Map<String, Integer> maxStoryPoints;
    private int avgHoursPerStoryPoint;
}
