package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

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

    public static class EffortDetails {
        private double effort;
        private int heads_needed;
        private double story_points;

        public double getEffort() {
            return effort;
        }

        public void setEffort(double effort) {
            this.effort = effort;
        }

        public int getHeads_needed() {
            return heads_needed;
        }

        public void setHeads_needed(int heads_needed) {
            this.heads_needed = heads_needed;
        }

        public double getStory_points() {
            return story_points;
        }

        public void setStory_points(double story_points) {
            this.story_points = story_points;
        }
    }
}
