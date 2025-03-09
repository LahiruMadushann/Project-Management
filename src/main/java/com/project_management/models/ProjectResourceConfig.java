package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "project_resource_config")
public class ProjectResourceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private Boolean cloud;
    private Boolean db;
    private Boolean automation;
    private Boolean security;
    private Boolean collaboration;
    private Boolean ide;
    private BigDecimal resourceBudget;

}
