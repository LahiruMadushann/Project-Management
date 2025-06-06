package com.project_management.models;

import com.project_management.models.constant.PriorityValue;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "release_versions")
public class ReleaseVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String versionName;

    @Column(name = "create_user_id", nullable = false)
    private Long createUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "version_description")
    private String versionDescription;

    @OneToMany(mappedBy = "releaseVersion", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @Column(name = "version_limit_constant", nullable = false)
    private int versionLimitConstant = PriorityValue.PRIORITY_VALUE;

    public void reduceLimit(int difficultyLevel) {
        this.versionLimitConstant = Math.max(0, this.versionLimitConstant - difficultyLevel);
    }
}

