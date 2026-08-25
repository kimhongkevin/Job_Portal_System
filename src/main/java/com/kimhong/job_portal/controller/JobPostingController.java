package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.PageResponse;
import com.kimhong.job_portal.entity.ExperienceLevel;
import com.kimhong.job_portal.entity.JobType;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Admin manages ALL job postings — there is no employer ownership anymore.
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Posting", description = "Job posting endpoint")
@RequiredArgsConstructor
public class JobPostingController {
    private final JobPostingService jobPostingService;

    @PostMapping
    @Operation(summary="Admin create new job posting for a company")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponse> createJob(
            @Valid @RequestBody JobPostingRequest request){

        return ResponseEntity.ok(jobPostingService.createJob(request));
    }

    @GetMapping("/{id}")
    @Operation(summary="View job by ID (everyone can access)")
    public ResponseEntity<JobPostingResponse> getJobById(@PathVariable Long id){

        return ResponseEntity.ok(jobPostingService.getJobById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary="Admin update job posting info")
    @PreAuthorize("hasRole('ADMIN')")
    public  ResponseEntity<JobPostingResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingRequest request){

        return ResponseEntity.ok(jobPostingService.updateJob(id,request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Admin delete job posting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id){

        jobPostingService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    private static final List<String> ALLOWED_SORT_FIELDS= List.of("createdAt","salary","location","title");

    private Pageable buildPageable(int page,int size,String sortBy,String sortDir){
        if(!ALLOWED_SORT_FIELDS.contains(sortBy))
            throw new BadRequestException("Invalid sort field. Allowed: "+ ALLOWED_SORT_FIELDS);

        if(!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc"))
            throw new BadRequestException("Sort direction must be 'asc' or 'desc' ");

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        return PageRequest.of(page,size,sort);
    }

    @GetMapping("/open/paged")
    @Operation(summary="View all open jobs — only JOB_BOARD jobs are public (everyone can access)")
    public ResponseEntity<PageResponse<JobPostingResponse>> getAllOpenJobsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){

        return ResponseEntity.ok(jobPostingService.getAllOpenJobsPaginated(
                buildPageable(page,size,sortBy,sortDir)
        ));
    }

    @GetMapping("/search/paged")
    @Operation(summary="Search jobs by keyword,location,job type — TALENT_POOL jobs excluded (everyone can access)")
    public ResponseEntity<PageResponse<JobPostingResponse>> searchJob(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
            ){

        return ResponseEntity.ok(jobPostingService.searchJobs(keyword,location,jobType,categoryId,experienceLevel,
                minSalary,maxSalary,buildPageable(page,size,sortBy,sortDir)));

    }

    @PatchMapping("/close/{id}")
    @Operation(summary="Admin close job posting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobPostingResponse> closeJob(@PathVariable Long id){
        return ResponseEntity.ok(jobPostingService.closeJob(id));
    }

}
