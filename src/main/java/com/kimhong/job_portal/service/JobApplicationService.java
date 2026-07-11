package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
import com.kimhong.job_portal.dto.UpdateApplicationStatusRequest;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.exception.UnauthorizedException;
import com.kimhong.job_portal.repository.JobApplicationRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import com.kimhong.job_portal.repository.SeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final EmailService emailService;

    private JobApplicationResponse mapToJobApplicationResponse(JobApplication application){
        return new JobApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompanyName(),
                application.getSeeker().getId(),
                application.getSeeker().getUser().getEmail(),
                application.getStatus(),
                application.getCoverLetter(),
                application.getAppliedAt()
        );
    }

    public JobApplicationResponse applyToJob(JobApplicationRequest request,String email){
        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found, please create your profile first"));
        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));
        if(job.getJobStatus() != JobStatus.OPEN)
            throw new BadRequestException("Cannot apply to a closed job.");

        if(jobApplicationRepository.existsBySeekerAndJob(profile,job))
            throw new DuplicateResourceException("Already applied to this job.");

        JobApplication application = JobApplication.builder()
                        .seeker(profile)
                        .job(job)
                        .status(ApplicationStatus.PENDING)
                        .coverLetter(request.getCoverLetter())
                        .build();
        JobApplication saved = jobApplicationRepository.save(application);

        emailService.sendApplicationConfirmation(
                profile.getUser().getEmail(),
                profile.getUser().getFullName(),
                job.getTitle(),
                job.getEmployer().getCompanyName());

        return mapToJobApplicationResponse(saved);
    }

    public List<JobApplicationResponse> getMyApplication(String email){
        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found, please create your profile first"));

        return jobApplicationRepository.findBySeeker(profile).stream()
                .map(this::mapToJobApplicationResponse).toList();
    }

    // Employer views all application for a specific job
    public List<JobApplicationResponse> getApplicationForJob(Long jobId,String email){
       JobPosting job = jobPostingRepository.findById(jobId)
               .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
       if(!job.getEmployer().getUser().getEmail().equals(email))
           throw new UnauthorizedException("Unauthorized");

       return jobApplicationRepository.findByJob(job).stream()
               .map(this::mapToJobApplicationResponse).toList();
    }

    // Employer update application status
    public JobApplicationResponse updateApplicationStatus(
            Long applicationId,
            UpdateApplicationStatusRequest request,
            String email){

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if(!application.getJob().getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        application.setStatus(request.getStatus());

        JobApplication saved = jobApplicationRepository.save(application);
        emailService.sendApplicationStatusUpdate(
                application.getSeeker().getUser().getEmail(),
                application.getSeeker().getUser().getFullName(),
                application.getJob().getTitle(),
                request.getStatus()
        );

        return mapToJobApplicationResponse(saved);
    }

    // Seeker withdraws application (only if PENDING)
    public void withdrawApplication(Long applicationId,String email){
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
        if(!application.getSeeker().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");
        if(application.getStatus() != ApplicationStatus.PENDING)
            throw new BadRequestException("Cannot withdraw application that is already being processed");
        jobApplicationRepository.delete(application);
    }

    // Employer filters application by status
    public List<JobApplicationResponse> getApplicationByStatus(
            Long jobId,
            ApplicationStatus status,
            String email){

        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        return jobApplicationRepository.findByJobAndStatus(job,status).stream()
                .map(this::mapToJobApplicationResponse).toList();
    }
}
