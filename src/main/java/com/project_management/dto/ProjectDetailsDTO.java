package com.project_management.dto;

import com.project_management.models.enums.ProjectStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDetailsDTO {
    private Long id;
    private String name;
    private String summary;
    private String domain;
    private BigDecimal budget;
    private LocalDate deadline;
    private ProjectStatus status;
    private BigDecimal predictedBudget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReleaseVersionDTONew> releaseVersions;
}
