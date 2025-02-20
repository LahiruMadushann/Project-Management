package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "advance_details")
@Data
@NoArgsConstructor
public class AdvanceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String domain;
    private Integer methodology;

    private Integer programmingLang;

    private Integer dbms;

    private Boolean devops;
    private Boolean integration;
    private Boolean ml;
    private Integer securityLevel;
    private Integer userCount;
    private Integer duration;
    private Integer scheduleQuality;
    private Integer standard;
    private Integer requirementAccuracy;
    private Integer documentation;
    private Double expectedProfit;
    private Double otherExpenses;
}
