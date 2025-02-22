package com.project_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "effort_details")
public class EffortDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_category_name")
    private String subCategoryName;

    @Column(name = "effort")
    private double effort;

    @Column(name = "heads_needed")
    private int headsNeeded;

    @Column(name = "story_points")
    private double storyPoints;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
    }

    public int getHeadsNeeded() {
        return headsNeeded;
    }

    public void setHeadsNeeded(int headsNeeded) {
        this.headsNeeded = headsNeeded;
    }

    public double getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(double storyPoints) {
        this.storyPoints = storyPoints;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }
}

