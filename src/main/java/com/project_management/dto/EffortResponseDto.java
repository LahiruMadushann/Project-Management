package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EffortResponseDto {

    @JsonProperty("predicted_effort")
    private Double effort;
    @JsonProperty("success")
    private Boolean success;
}
