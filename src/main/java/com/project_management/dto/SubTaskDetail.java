package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SubTaskDetail {
    @JsonProperty("estimated_hours")
    private double estimatedHours;
    @JsonProperty("subtask_name")
    private String subtaskName;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
}
