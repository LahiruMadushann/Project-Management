package com.project_management.repositories;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.Project;
import com.project_management.models.enums.RoleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
