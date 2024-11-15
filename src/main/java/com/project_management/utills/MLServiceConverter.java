package com.project_management.utills;

import com.project_management.dto.MLAnalysisRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MLServiceConverter {
    public static Map<String, Object> convertToMLServiceFormat(MLAnalysisRequest request) {
        List<Map<String, String>> convertedStories = request.getSimpleUserStories().stream()
                .map(story -> {
                    Map<String, String> convertedStory = new HashMap<>();
                    convertedStory.put("user_type", story.getUserType());
                    convertedStory.put("action", story.getAction());
                    convertedStory.put("what", story.getWhat());
                    convertedStory.put("context", story.getContext());
                    return convertedStory;
                })
                .collect(Collectors.toList());

        Map<String, Object> mlRequest = new HashMap<>();
        mlRequest.put("simple_user_stories", convertedStories);
        return mlRequest;
    }
}
