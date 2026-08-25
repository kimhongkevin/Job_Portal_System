package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.PageResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.CompanyProfileRepository;
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
    private final CompanyProfileRepository companyProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;

    // Public jobs must belong to the JOB_BOARD recruitment model
    private static final RecruitmentModel PUBLIC_RECRUITMENT_MODEL = RecruitmentModel.JOB_BOARD;

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
                jobPosting.getRecruitmentModel(),
                jobPosting.getCompany() != null ? jobPosting.getCompany().getId() : null,
                jobPosting.getCompany() != null ? jobPosting.getCompany().getCompanyName() : null,
                jobPosting.getCreatedAt()
        );
    }


    // Admin creates a job posting directly for a company (no ownership concept)
    public JobPostingResponse createJob(JobPostingRequest request){
        CompanyProfile company = companyProfileRepository.findById(request.getCompanyId())
                .orElseThrow(()-> new ResourceNotFoundException("Company not found"));

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
                .recruitmentModel(request.getRecruitmentModel() != null
                        ? request.getRecruitmentModel()
                        : RecruitmentModel.JOB_BOARD)
                .company(company)
                .build();
        return mapToJobPostingResponse(jobPostingRepository.save(job));
    }

    // Admin views all jobs (replaces the old employer-only GET /api/jobs/my)
    public List<JobPostingResponse> getAllJobs(){
        return jobPostingRepository.findAll().stream()
                .map(this::mapToJobPostingResponse).toList();
    }

    public JobPostingResponse getJobById(Long id){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        return mapToJobPostingResponse(job);
    }

    public JobPostingResponse updateJob(Long id,JobPostingRequest request){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));

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

        if(request.getRecruitmentModel() != null)
            job.setRecruitmentModel(request.getRecruitmentModel());

        if(request.getCompanyId() != null){
            CompanyProfile company = companyProfileRepository.findById(request.getCompanyId())
                    .orElseThrow(()-> new ResourceNotFoundException("Company not found"));
            job.setCompany(company);
        }

        if(request.getCategoryId() != null){
            JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
            job.setCategory(category);
        }

        return  mapToJobPostingResponse(jobPostingRepository.save(job));
    }

    public void deleteJob(Long id){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        jobPostingRepository.delete(job);
    }

    // Public listing: only OPEN + JOB_BOARD jobs are visible to everyone
    public PageResponse<JobPostingResponse> getAllOpenJobsPaginated(Pageable pageable){

        return PageResponse.of(
                jobPostingRepository.findByJobStatusAndRecruitmentModel(
                                JobStatus.OPEN, PUBLIC_RECRUITMENT_MODEL, pageable)
                        .map(this::mapToJobPostingResponse)
        );
    }

    // Public search: TALENT_POOL jobs excluded automatically by the query
    public PageResponse<JobPostingResponse> searchJobs(
            String keyword,
            String location,
            JobType jobType,
            Long categoryId,
            ExperienceLevel experienceLevel,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Pageable pageable){

        JobCategory category = null;
        if(categoryId != null){
            category = jobCategoryRepository.findById(categoryId)
                    .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
        }

        return PageResponse.of(jobPostingRepository.searchJobs(keyword, location, jobType, category,experienceLevel,minSalary,maxSalary,pageable)
                .map(this::mapToJobPostingResponse));
    }

    public JobPostingResponse closeJob(Long id){
        JobPosting job = jobPostingRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Job not found."));
        job.setJobStatus(JobStatus.CLOSED);

        return mapToJobPostingResponse(jobPostingRepository.save(job));
    }

}
