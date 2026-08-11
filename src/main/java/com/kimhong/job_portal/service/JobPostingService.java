package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.PageResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.exception.UnauthorizedException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobCategoryRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final UserService userService;

    private String computeSalaryDisplay(BigDecimal minSalary,BigDecimal maxSalary){
        if(minSalary == null && maxSalary == null){
            return "Negotiable";
        }
        if(minSalary != null && maxSalary != null){
            if(maxSalary.compareTo(minSalary) == 0) {
                return "$" + minSalary;
            }
            return "$"+minSalary +" - $"+maxSalary;
        }
        if(minSalary != null){
            return "From $"+minSalary;
        }
        return "Up to $"+maxSalary;
    }

    private JobPostingResponse mapToJobPostingResponse(JobPosting jobPosting){
        String salaryDisplay = computeSalaryDisplay(jobPosting.getMinSalary(),jobPosting.getMaxSalary());

        return new JobPostingResponse(
                jobPosting.getId(),
                jobPosting.getTitle(),
                jobPosting.getDescription(),
                jobPosting.getCategory() != null ? jobPosting.getCategory().getId() : null,
                jobPosting.getCategory() != null ? jobPosting.getCategory().getName() : null,
                jobPosting.getRequirement(),
                jobPosting.getQualification(),
                jobPosting.getBenefits(),
                jobPosting.getExperienceLevel(),
                jobPosting.getDeadline(),
                jobPosting.getLocation(),
                jobPosting.getJobType(),
                jobPosting.getMinSalary(),
                jobPosting.getMaxSalary(),
                salaryDisplay,
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

        // If category is provided
        JobCategory category = null;
        if(request.getCategoryId() != null)
            category = jobCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(()-> new ResourceNotFoundException("Category not found"));

        if(request.getRequirement() == null || request.getRequirement().isBlank())
            throw new BadRequestException("Requirement is required");

        if(request.getQualification() == null || request.getQualification().isBlank())
            throw new BadRequestException("Qualification is required");

        JobPosting job = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .requirement(request.getRequirement())
                .qualification(request.getQualification())
                .deadline(request.getDeadline())
                .experienceLevel(request.getExperienceLevel())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
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

        if(request.getRequirement() != null && !request.getRequirement().isBlank())
            job.setRequirement(request.getRequirement());

        if(request.getQualification() != null && !request.getQualification().isBlank())
            job.setQualification(request.getQualification());

        if(request.getBenefits() != null && !request.getBenefits().isBlank())
            job.setBenefits(request.getBenefits());

        if(request.getExperienceLevel() != null)
            job.setExperienceLevel(request.getExperienceLevel());

        if(request.getDeadline() != null)
            job.setDeadline(request.getDeadline());

        if(request.getLocation() != null && !request.getLocation().isBlank())
            job.setLocation(request.getLocation());

        if(request.getJobType() != null)
            job.setJobType(request.getJobType());

        if(request.getMinSalary() != null)
            job.setMinSalary(request.getMinSalary());

        if(request.getCategoryId() != null){
            JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
            job.setCategory(category);
        }

        return  mapToJobPostingResponse(jobPostingRepository.save(job));
    }

    public void deleteJob(Long id, String email){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        jobPostingRepository.delete(job);
    }

    public PageResponse<JobPostingResponse> getAllOpenJobsPaginated(Pageable pageable){

        return PageResponse.of(
                jobPostingRepository.findByJobStatus(JobStatus.OPEN,pageable)
                        .map(this::mapToJobPostingResponse)
        );
    }

    public PageResponse<JobPostingResponse> searchJobs(
            String keyword,
            String location,
            JobType jobType,
            Long categoryId,
            ExperienceLevel experienceLevel,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Pageable pageable){

        JobCategory category;
        category = jobCategoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found"));

        return PageResponse.of(jobPostingRepository.searchJobs(keyword, location, jobType, category,experienceLevel,minSalary,maxSalary,pageable)
                .map(this::mapToJobPostingResponse));
    }

    public JobPostingResponse closeJob(Long id,String email){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        if(!job.getEmployer().getUser().getEmail().equals(email))
            throw new UnauthorizedException("Unauthorized");

        job.setJobStatus(JobStatus.CLOSED);

        return mapToJobPostingResponse(jobPostingRepository.save(job));
    }

}
