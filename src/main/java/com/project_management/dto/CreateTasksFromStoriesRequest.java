package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTasksFromStoriesRequest {
    @JsonProperty("simple_user_stories")
    private List<UserStory> simpleUserStories;
    private Long releaseVersionId;
    private Long createUserId;
    private Integer difficultyLevel;
    private Long assignedUserId;
    private LocalDate assignedDate;
    private LocalDate startDate;
    private LocalDate deadline;
    private LocalDate completedDate;
}