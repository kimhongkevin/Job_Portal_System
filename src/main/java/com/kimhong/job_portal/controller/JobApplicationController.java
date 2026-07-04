package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
import com.kimhong.job_portal.dto.UpdateApplicationStatusRequest;
import com.kimhong.job_portal.entity.ApplicationStatus;
import com.kimhong.job_portal.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @RequestBody JobApplicationRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.applyToJob(request, authentication.getName()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplication(Authentication authentication){
        return ResponseEntity.ok(jobApplicationService.getMyApplication(authentication.getName()));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationForJob(
            @PathVariable Long jobId,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.getApplicationForJob(jobId, authentication.getName()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(id,request, authentication.getName()));
    }

    @DeleteMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id,Authentication authentication){
        jobApplicationService.withdrawApplication(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/job/{jobId}/filter")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationByStatus(
            @PathVariable Long jobId,
            @RequestParam ApplicationStatus status,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.getApplicationByStatus(jobId,status, authentication.getName()));
    }

}
