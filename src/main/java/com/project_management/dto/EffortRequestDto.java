package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class EffortRequestDto {

    @JsonProperty("Schedule quality")
    private Integer scheduleQuality;
    @JsonProperty("Methodology")
    private Integer Methodology;
    @JsonProperty("# Multiple programing languages ")
    private Integer multiLang;
    @JsonProperty("Programming language used")
    private Integer programmingLang;
    @JsonProperty("DBMS used")
    private Integer dbms;
    @JsonProperty("Use of standards")
    private Integer standards;
    @JsonProperty("Requirement accuracy level")
    private Integer accuracy;
    @JsonProperty("Technical documentation")
    private Integer documentation;



//    {
//        "Schedule quality":2.0,
//            "Methodology": 2.0,
//            "# Multiple programing languages ": 2,
//            "Programming language used": 2,
//            "DBMS used": 2,
//            "Use of standards": 1.0,
//            "Requirement accuracy level": 1.0,
//            "Technical documentation": 2.0
//    }

}
