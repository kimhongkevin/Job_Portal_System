package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
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

// Admin no longer manages applications:
// applying automatically emails the CV to the company HR
// and flips the status to SENT.
@RestController
@RequestMapping("/api/applications")
@Tag(name = "Job Application", description = "Job application endpoint")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping
    @Operation(summary = "Apply for a job",
            description = "Requires an uploaded resume. The CV (resume PDF) is emailed " +
                    "automatically to the company HR contact.")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<JobApplicationResponse> applyToJob(
            @Valid @RequestBody JobApplicationRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobApplicationService.applyToJob(request, authentication.getName()));
    }

    @GetMapping("/my")
    @Operation(summary = "Seeker view their applications")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(Authentication authentication){
        return ResponseEntity.ok(jobApplicationService.getMyApplications(authentication.getName()));
    }

    @DeleteMapping("/{id}/withdraw")
    @Operation(summary ="Seeker withdraw application while it is still PENDING")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id,Authentication authentication){
        jobApplicationService.withdrawApplication(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

}
