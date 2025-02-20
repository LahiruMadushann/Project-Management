package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class BudgetMLRequestDto {

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("Methodology")
    private String methodology;

    @JsonProperty("Dev Count")
    private int devCount;

    @JsonProperty("QA Count")
    private int qaCount;

    @JsonProperty("BA Count")
    private int baCount;

    @JsonProperty("PM Count")
    private int pmCount;

    @JsonProperty("DevOps Count")
    private int devOpsCount;

    @JsonProperty("Resource_Cloud")
    private int resourceCloud;

    @JsonProperty("Resource_DB")
    private int resourceDb;

    @JsonProperty("Resource_Automation")
    private int resourceAutomation;

    @JsonProperty("Resource_Ide_tools")
    private int resourceIdeTools;

    @JsonProperty("Resource_Security")
    private int resourceSecurity;

    @JsonProperty("Resource_Collaboration")
    private int resourceCollaboration;

    @JsonProperty("Miscellaneous Expenses (%)")
    private Double miscellaneousExpenses;

    @JsonProperty("Profit Margin (%)")
    private Double profitMargin;

}
