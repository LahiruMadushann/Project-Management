package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserStory {
    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("action")
    private String action;

    @JsonProperty("what")
    private String what;

    @JsonProperty("context")
    private String context;
}
