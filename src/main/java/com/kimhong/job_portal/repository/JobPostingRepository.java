package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.JobPosting;
import com.kimhong.job_portal.entity.JobStatus;
import com.kimhong.job_portal.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting,Long> {

    // Find all jobs by a specific employer
    List<JobPosting> findByEmployer(EmployerProfile employer);

    // With Pagination

    // Paginated open jobs
    Page<JobPosting> findByJobStatus(JobStatus jobStatus, Pageable pageable);

    // Paginated job search
    @Query("""
    SELECT j FROM JobPosting j
    WHERE
    (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
    AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%')))
    AND (:jobType IS NULL OR j.jobType = :jobType)
    AND j.jobStatus = 'OPEN'
""")
    Page<JobPosting> searchJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            Pageable pageable
    );


}
