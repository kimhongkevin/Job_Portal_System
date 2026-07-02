package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.JobPosting;
import com.kimhong.job_portal.entity.JobStatus;
import com.kimhong.job_portal.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting,Long> {

    // Find all jobs by a specific employer
    List<JobPosting> findByEmployer(EmployerProfile employer);

    // Find all open jobs
    List<JobPosting> findByJobStatus(JobStatus jobStatus);

    // Search by title containing a keyword (case-insensitive)
    List<JobPosting> findByTitleContainingIgnoreCase(String keyword);

    // Filter by location
    List<JobPosting> findByLocationContainingIgnoreCase(String location);

    // Filter by job types
    List<JobPosting> findByJobType(JobType jobType);
}
