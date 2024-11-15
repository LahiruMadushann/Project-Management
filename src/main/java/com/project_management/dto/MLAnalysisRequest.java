package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MLAnalysisRequest {
    @JsonProperty("simple_user_stories")
    private List<UserStory> simpleUserStories;
}
