package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResourceMLRequestDTO {

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("Methodology")
    private String methodology;

    @JsonProperty("Is_MultiLang")
    private int isMultiLang;

    @JsonProperty("Is_MultiDB")
    private int isMultiDB;

    @JsonProperty("Is_DevOps")
    private int isDevOps;

    @JsonProperty("Data_Security_Level")
    private String dataSecurityLevel;

    @JsonProperty("User_Count_Estimate")
    private int userCountEstimate;

    @JsonProperty("Project_Size")
    private String projectSize;

    @JsonProperty("Expected_Data_Size")
    private int expectedDataSize;

    @JsonProperty("Integration_Required")
    private int integrationRequired;

    @JsonProperty("Machine_Learning_Enabled")
    private int machineLearningEnabled;

    @JsonProperty("Expected_Project_Duration")
    private int expectedProjectDuration;

    @JsonProperty("Cloud_Type")
    private String cloudType;

    @JsonProperty("Database_Type")
    private String databaseType;
}
