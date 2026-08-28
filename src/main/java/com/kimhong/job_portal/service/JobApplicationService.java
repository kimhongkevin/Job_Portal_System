package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
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
    private final SupabaseStorageService supabaseStorageService;

    private JobApplicationResponse mapToJobApplicationResponse(JobApplication application){
        return new JobApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany().getCompanyName(),
                application.getSeeker().getId(),
                application.getSeeker().getUser().getEmail(),
                application.getStatus(),
                application.getCoverLetter(),
                application.getAppliedAt()
        );
    }

    // Automatic flow:
    // 1. Validate seeker profile + uploaded resume
    // 2. Validate job is OPEN and not already applied
    // 3. Save application (PENDING)
    // 4. Email the CV (resume PDF attached) to company HR (contactEmail)
    // 5. On success -> status becomes SENT automatically
    //    On failure -> status stays PENDING (can be retried)
    // 6. Send confirmation email to the seeker
    public JobApplicationResponse applyToJob(JobApplicationRequest request,String email){
        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found, please create your profile first"));

        // Resume is mandatory — it is attached to the CV email sent to the company HR
        if(profile.getResumeUrl() == null || profile.getResumeUrl().isBlank())
            throw new BadRequestException("Please upload your resume before applying");

        JobPosting job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));
        if(job.getJobStatus() != JobStatus.OPEN)
            throw new BadRequestException("Cannot apply to a closed job.");

        if(jobApplicationRepository.existsBySeekerAndJob(profile,job))
            throw new DuplicateResourceException("Already applied to this job.");

        CompanyProfile company = job.getCompany();

        // Save as PENDING first; flips to SENT once the CV email is delivered
        JobApplication saved = jobApplicationRepository.save(JobApplication.builder()
                        .seeker(profile)
                        .job(job)
                        .status(ApplicationStatus.PENDING)
                        .coverLetter(request.getCoverLetter())
                        .build());

        String fileName = supabaseStorageService.extractFileName(profile.getResumeUrl());
        String signedResumeUrl = supabaseStorageService.getSignedUrl("resumes",fileName,3600);

        // Fallback to original URL if signed URL generation fails
        String finalResumeUrl = (signedResumeUrl != null) ? signedResumeUrl : profile.getResumeUrl();

        boolean cvDelivered = emailService.sendCVToCompanyHR(
                company.getContactEmail(),
                null, // optional CC — could be an admin notification address
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                job.getTitle(),
                company.getCompanyName(),
                finalResumeUrl,
                request.getCoverLetter());

        if(cvDelivered){
            saved.setStatus(ApplicationStatus.SENT);
            saved = jobApplicationRepository.save(saved);
        }

        emailService.sendCVSentConfirmation(
                profile.getUser().getEmail(),
                profile.getUser().getFullName(),
                job.getTitle(),
                company.getCompanyName(),
                company.getContactEmail());

        return mapToJobApplicationResponse(saved);
    }

    public List<JobApplicationResponse> getMyApplications(String email){
        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found, please create your profile first"));

        return jobApplicationRepository.findBySeeker(profile).stream()
                .map(this::mapToJobApplicationResponse).toList();
    }

    // Seeker withdraws application (only while still PENDING,
    // i.e. when the CV email has not been delivered yet)
    public void withdrawApplication(Long applicationId,String email){
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));
        if(!application.getSeeker().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");
        if(application.getStatus() != ApplicationStatus.PENDING)
            throw new BadRequestException("Cannot withdraw application that is already being processed");
        jobApplicationRepository.delete(application);
    }
}
