package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.SeekerProfileRequest;
import com.kimhong.job_portal.dto.SeekerProfileResponse;
import com.kimhong.job_portal.service.SeekerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/seeker/profile")
@Tag(name = "Job Seekers", description = "Job seeker endpoint")
@RequiredArgsConstructor
public class SeekerProfileController {
    private final SeekerProfileService seekerProfileService;

    @PostMapping
    @Operation(summary="Seeker create new profile")
    public ResponseEntity<SeekerProfileResponse> createProfile(
            @RequestBody SeekerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.createProfile(request,authentication.getName()));
    }

    @GetMapping
    @Operation(summary="Seeker views profile")
    public ResponseEntity<SeekerProfileResponse> getMyProfile(Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.getMyProfile(authentication.getName()));
    }

    @PutMapping
    @Operation(summary="Seeker updates profile")
    public ResponseEntity<SeekerProfileResponse> updateMyProfile(
            @Valid @RequestBody SeekerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(seekerProfileService.updateProfile(request, authentication.getName()));
    }

    @PostMapping("/resume")
    @Operation(summary="Seeker uploads resume. If has existing, it replace the previous resume")
    public ResponseEntity<SeekerProfileResponse> uploadResume(
            @Parameter(description = "PDF file to upload", required = true)
            @RequestParam("file") MultipartFile file, // @RequestParam("file") not @RequestBody — file uploads use multipart/form-data, not JSON.
            Authentication authentication
            ) {
        return ResponseEntity.ok(seekerProfileService.uploadResume(file, authentication.getName()));
    }
}
