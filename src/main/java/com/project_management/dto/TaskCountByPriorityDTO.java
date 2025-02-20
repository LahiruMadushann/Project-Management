package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCountByPriorityDTO {
    @JsonProperty("count")
    private Long count;

    @JsonProperty("priority")
    private String priority;

    public TaskCountByPriorityDTO() {}

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
