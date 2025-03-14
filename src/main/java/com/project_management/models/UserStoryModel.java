package com.project_management.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_story")
public class UserStoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("action")
    private String action;

    @JsonProperty("what")
    private String what;

    @JsonProperty("context")
    private String context;

    private Long userId;
    private Long releaseId;
}
