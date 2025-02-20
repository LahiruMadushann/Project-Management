package com.project_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdvanceDetailsDTO {

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
