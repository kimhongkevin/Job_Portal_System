package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.JobPosting;
import com.kimhong.job_portal.entity.JobStatus;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.exception.UnauthorizedException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final UserService userService;

    private JobPostingResponse mapToJobPostingResponse(JobPosting jobPosting){
        return new JobPostingResponse(
                jobPosting.getId(),
                jobPosting.getTitle(),
                jobPosting.getDescription(),
                jobPosting.getLocation(),
                jobPosting.getJobType(),
                jobPosting.getSalary(),
                jobPosting.getJobStatus(),
                jobPosting.getEmployer().getId(),
                jobPosting.getEmployer().getCompanyName(),
                jobPosting.getCreatedAt()
        );
    }

    public JobPostingResponse createJob(JobPostingRequest request, String email){
        User user = userService.getUserByEmail(email);
        EmployerProfile profile = employerProfileRepository.findByUser(user)
                .orElseThrow(()-> new ResourceNotFoundException("Employer profile not found, please create your profile first"));
        JobPosting job = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .salary(request.getSalary())
                .jobStatus(JobStatus.OPEN)
                .employer(profile)
                .build();
        return mapToJobPostingResponse(jobPostingRepository.save(job));
    }

    public List<JobPostingResponse> getMyJobPosting(String email){
        User user = userService.getUserByEmail(email);
        EmployerProfile profile = employerProfileRepository.findByUser(user)
                .orElseThrow(()-> new ResourceNotFoundException("Employer profile not found, please create your profile first"));

        return jobPostingRepository.findByEmployer(profile).stream()
                .map(this::mapToJobPostingResponse).toList();
    }

    public JobPostingResponse getJobById(Long id){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        return mapToJobPostingResponse(job);
    }

    public JobPostingResponse updateJob(Long id,JobPostingRequest request, String email){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        if(request.getTitle() != null && !request.getTitle().isBlank())
            job.setTitle(request.getTitle());

        if(request.getDescription() != null && !request.getDescription().isBlank())
            job.setDescription(request.getDescription());

        if(request.getLocation() != null && !request.getLocation().isBlank())
            job.setLocation(request.getLocation());

        if(request.getJobType() != null)
            job.setJobType(request.getJobType());

        if(request.getSalary() != null)
            job.setSalary(request.getSalary());

        return  mapToJobPostingResponse(jobPostingRepository.save(job));
    }

    public void deleteJob(Long id, String email){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        jobPostingRepository.delete(job);
    }

    public List<JobPostingResponse> getAllOpenJobs(){
        return jobPostingRepository.findByJobStatus(JobStatus.OPEN).stream()
                .map(this::mapToJobPostingResponse).toList();
    }

    public List<JobPostingResponse> searchByTitle(String keyword){
        return jobPostingRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(this::mapToJobPostingResponse).toList();
    }

    public JobPostingResponse closeJob(Long id,String email){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        job.setJobStatus(JobStatus.CLOSED);

        return mapToJobPostingResponse(jobPostingRepository.save(job));
    }






}
