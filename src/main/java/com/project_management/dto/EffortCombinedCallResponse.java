package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class EffortCombinedCallResponse {
    private EffortResponseDto effort;
    private ResourceMLResponseDTO resources;
    private CalculateMLResponseDTO calculateHeadRequest;
}
