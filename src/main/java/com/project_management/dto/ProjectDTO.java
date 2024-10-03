package com.project_management.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String summary;
    private String domain;
    private BigDecimal budget;
    private LocalDate deadline;
    private Long createUserId;
}
