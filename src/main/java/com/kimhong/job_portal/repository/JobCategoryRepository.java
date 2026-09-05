package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository <JobCategory,Long> {
    boolean existsByName(String name);
    Optional<JobCategory> findByName(String name);
}
