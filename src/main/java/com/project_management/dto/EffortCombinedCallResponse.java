package com.project_management.dto;

import lombok.Data;

@Data
public class EffortCombinedCallResponse {
    private EffortResponseDto effort;
    private ResourceMLResponseDTO resources;
}
