package com.project_management.repositories;

import com.project_management.models.PerfectEmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerfectEmployeeSkillRepository extends JpaRepository<PerfectEmployeeSkill, Long> {

    @Query("SELECT s FROM PerfectEmployeeSkill s")
    List<PerfectEmployeeSkill> findAllSkills();
}
