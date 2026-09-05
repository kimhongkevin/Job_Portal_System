package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface JobPostingRepository extends JpaRepository<JobPosting,Long> {

    @Query("""
        SELECT COUNT(j) FROM JobPosting j
        WHERE j.company = :company AND j.jobStatus = 'OPEN' AND j.recruitmentModel = 'JOB_BOARD'
""")
    Long countOpenJobsByCompany(@Param("company") CompanyProfile company);

    @Query("""
        SELECT COUNT(j) FROM JobPosting j
        WHERE j.category = :category AND j.jobStatus = 'OPEN' AND j.recruitmentModel = 'JOB_BOARD'
""")
    Long countOpenJobsByCategory(@Param("category") JobCategory category);



    // With Pagination

    // Paginated public open jobs (only JOB_BOARD jobs are public)
    Page<JobPosting> findByJobStatusAndRecruitmentModel(
            JobStatus jobStatus, RecruitmentModel recruitmentModel, Pageable pageable);

    // Paginated job search
    @Query("""
    SELECT j FROM JobPosting j
    WHERE
    (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
    AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%')))
    AND (:jobType IS NULL OR j.jobType = :jobType)
    AND (:category IS NULL OR j.category = :category)
    AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
    AND (:minSalary IS NULL OR j.minSalary IS NULL OR j.minSalary = :minSalary)
    AND (:maxSalary IS NULL OR j.maxSalary IS NULL OR j.maxSalary = :maxSalary)
    AND j.jobStatus = 'OPEN'
    AND j.recruitmentModel = 'JOB_BOARD'
    AND (j.deadline IS NULL OR j.deadline >= CURRENT_DATE)
""")
    Page<JobPosting> searchJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("category") JobCategory jobCategory,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary,
            Pageable pageable
    );


}
