package com.project_management.models;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "calculation_result_id")
    private CalculationResult calculationResult;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "subCategoryName")
    private Map<String, EffortDetailsEntity> subCategories = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public CalculationResult getCalculationResult() {
        return calculationResult;
    }

    public void setCalculationResult(CalculationResult calculationResult) {
        this.calculationResult = calculationResult;
    }

    public Map<String, EffortDetailsEntity> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(Map<String, EffortDetailsEntity> subCategories) {
        this.subCategories = subCategories;
    }
}

