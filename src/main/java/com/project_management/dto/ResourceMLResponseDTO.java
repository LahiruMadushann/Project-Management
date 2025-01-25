package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class ResourceMLResponseDTO {

    @JsonProperty("Resource_Automation")
    private ResourcePrediction resourceAutomation;

    @JsonProperty("Resource_Cloud")
    private ResourcePrediction resourceCloud;

    @JsonProperty("Resource_Collaboration")
    private ResourcePrediction resourceCollaboration;

    @JsonProperty("Resource_DB")
    private ResourcePrediction resourceDB;

    @JsonProperty("Resource_Ide_tools")
    private ResourcePrediction resourceIdeTools;

    @JsonProperty("Resource_Security")
    private ResourcePrediction resourceSecurity;

    @Data
    public static class ResourcePrediction {
        private boolean prediction;
        private double probability;
    }
}
