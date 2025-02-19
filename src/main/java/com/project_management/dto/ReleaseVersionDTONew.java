package com.project_management.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReleaseVersionDTONew {
    private Long id;
    private String versionName;
    private String versionDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int versionLimitConstant;
    private List<TaskDTO> tasks;
}
