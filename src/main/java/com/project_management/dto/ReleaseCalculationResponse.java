package com.project_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseCalculationResponse {
    private Double totalEffort;
    private Map<String, Integer> tasks;
    private Map<String, Map<String, Double>> roleDistribution;
    private Map<String, Integer> maxStoryPoints;
    private int avgHoursPerStoryPoint;

    public static ReleaseCalculationResponse create() {
        return new ReleaseCalculationResponse();
    }


}
