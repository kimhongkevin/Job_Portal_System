package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.JobApplication;
import com.kimhong.job_portal.entity.JobPosting;
import com.kimhong.job_portal.entity.SeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication,Long> {
    // All application by a specific seeker
    List<JobApplication> findBySeeker(SeekerProfile seekerProfile);

    // Check if seeker already applied for this job (prevent duplication)
    boolean existsBySeekerAndJob(SeekerProfile seeker,JobPosting job);

    // Find a specific application by seeker + job
    Optional<JobApplication> findBySeekerAndJob(SeekerProfile seeker,JobPosting job);

}
