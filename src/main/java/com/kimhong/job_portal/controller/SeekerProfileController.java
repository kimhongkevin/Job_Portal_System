package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.SeekerProfileRequest;
import com.kimhong.job_portal.dto.SeekerProfileResponse;
import com.kimhong.job_portal.service.SeekerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/seeker/profile")
@RequiredArgsConstructor
public class SeekerProfileController {
    private final SeekerProfileService seekerProfileService;

    @PostMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<SeekerProfileResponse> createProfile(
            @RequestBody SeekerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.createProfile(request,authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<SeekerProfileResponse> getMyProfile(Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.getMyProfile(authentication.getName()));
    }

    @PutMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<SeekerProfileResponse> updateMyProfile(
            @RequestBody SeekerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.updateProfile(request, authentication.getName()));
    }
}
