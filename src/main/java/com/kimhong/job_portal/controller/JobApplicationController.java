package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
import com.kimhong.job_portal.dto.UpdateApplicationStatusRequest;
import com.kimhong.job_portal.entity.ApplicationStatus;
import com.kimhong.job_portal.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Job Application", description = "Job application endpoint")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping
    @Operation(summary = "Apply for a job")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @Valid @RequestBody JobApplicationRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.applyToJob(request, authentication.getName()));
    }

    @GetMapping("/my")
    @Operation(summary = "Seeker view their application")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplication(Authentication authentication){
        return ResponseEntity.ok(jobApplicationService.getMyApplication(authentication.getName()));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Employer views all application for a specific job")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationForJob(
            @PathVariable Long jobId,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.getApplicationForJob(jobId, authentication.getName()));
    }

    @PutMapping("/{applicationId}/status")
    @Operation(summary = "Employer update status(e.g PENDING,REVIEWED,ACCEPTED,REJECTED) on any application")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(applicationId,request,authentication.getName()));
    }

    @DeleteMapping("/{id}/withdraw")
    @Operation(summary ="Seeker withdraw application in case application status is pending")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id,Authentication authentication){
        jobApplicationService.withdrawApplication(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/job/{jobId}/filter")
    @Operation(summary="Employer filter applications by status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationByStatus(
            @PathVariable Long jobId,
            @RequestParam ApplicationStatus status,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.getApplicationByStatus(jobId,status, authentication.getName()));
    }

}
