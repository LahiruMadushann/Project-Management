package com.project_management.repositories;

import com.project_management.models.ReleaseVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long> {}
