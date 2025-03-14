package com.project_management.dto;

import com.project_management.models.UserStoryModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserStoryListResponseDto {
    private Map<Long,Map<Long, List<UserStoryModel>>> userStories;
}
