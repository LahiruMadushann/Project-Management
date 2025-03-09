package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateMLResponseDTO {
    private Map<String, Category> categories = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Category> getCategories() {
        return categories;
    }

    @JsonAnySetter
    public void setCategory(String key, Category value) {
        categories.put(key, value);
    }

    public static class Category {
        private Map<String, EffortDetails> subCategories = new HashMap<>();

        @JsonAnyGetter
        public Map<String, EffortDetails> getSubCategories() {
            return subCategories;
        }

        @JsonAnySetter
        public void setSubCategory(String key, EffortDetails value) {
            subCategories.put(key, value);
        }
    }

    @Data
    public static class EffortDetails {
        private double effort;
        private int headsNeeded = new Random().nextInt(10) + 1;
        private double story_points;
    }
}
