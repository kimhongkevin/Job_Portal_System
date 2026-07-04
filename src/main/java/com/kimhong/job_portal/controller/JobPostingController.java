package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {
    private final JobPostingService jobPostingService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobPostingResponse> createJob(
            @RequestBody JobPostingRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobPostingService.createJob(request,authentication.getName()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobPostingResponse>> getMyJobs(Authentication authentication){

        return ResponseEntity.ok(jobPostingService.getMyJobPosting(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getJobById(@PathVariable Long id){

        return ResponseEntity.ok(jobPostingService.getJobById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public  ResponseEntity<JobPostingResponse> updateJob(
            @PathVariable Long id,
            @RequestBody JobPostingRequest request,
            Authentication authentication){

        return ResponseEntity.ok(jobPostingService.updateJob(id,request,authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            Authentication authentication){

        jobPostingService.deleteJob(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/open")
    public ResponseEntity<List<JobPostingResponse>> getAllOpenJobs(){
        return ResponseEntity.ok(jobPostingService.getAllOpenJobs());
    }

    @GetMapping("/search")
    public  ResponseEntity<List<JobPostingResponse>> searchJob(
            @RequestParam String keyword){
        return ResponseEntity.ok(jobPostingService.searchByTitle(keyword));
    }

    @PatchMapping("/close/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobPostingResponse> closeJob(
            @PathVariable Long id,
            Authentication authentication){
        return ResponseEntity.ok(jobPostingService.closeJob(id,authentication.getName()));
    }
}
