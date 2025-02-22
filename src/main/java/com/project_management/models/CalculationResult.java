package com.project_management.models;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "calculation_results")
public class CalculationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @OneToMany(mappedBy = "calculationResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "categoryName")
    private Map<String, CategoryEntity> categories = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Map<String, CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryEntity> categories) {
        this.categories = categories;
    }
}
