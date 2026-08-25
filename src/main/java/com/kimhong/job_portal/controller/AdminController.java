package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.SeekerProfileResponse;
import com.kimhong.job_portal.service.JobPostingService;
import com.kimhong.job_portal.service.SeekerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin management endpoints")
@RequiredArgsConstructor
public class AdminController {
    private final JobPostingService jobPostingService;
    private final SeekerProfileService seekerProfileService;

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin views all job postings (replaces the old GET /api/jobs/my)")
    public ResponseEntity<List<JobPostingResponse>> getAllJobs(){
        return ResponseEntity.ok(jobPostingService.getAllJobs());
    }

    @GetMapping("/talent-pool")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search students who opted into the talent pool",
            description = "skills: comma separated keywords matched case-insensitively. " +
                    "available=true additionally requires an uploaded resume.")
    public ResponseEntity<List<SeekerProfileResponse>> searchTalentPool(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Boolean available){

        return ResponseEntity.ok(seekerProfileService.searchTalentPool(skills, available));
    }
}