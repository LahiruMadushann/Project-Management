package com.project_management.repositories;

import com.project_management.models.UserStoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStoryModel,Long> {
    List<UserStoryModel> findAllByReleaseId(Long id);
}
