package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.service.EmployerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/profile")
@Tag(name = "Employer Profiles", description = "Employer profile endpoint")
@RequiredArgsConstructor
public class EmployerProfileController {
    private final EmployerProfileService employerProfileService;

    @PostMapping
    @Operation(summary = "Create a new profile for employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerProfileResponse> createProfile(
            @Valid @RequestBody EmployerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(employerProfileService.createProfile(request, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "View profile for employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerProfileResponse> getMyProfile(Authentication authentication){

        return ResponseEntity.ok(employerProfileService.getMyProfile(authentication.getName()));
    }

    @PutMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update profile for employer")
    public ResponseEntity<EmployerProfileResponse> updateMyProfile(
            @Valid @RequestBody EmployerProfileRequest request,
            Authentication authentication){
        return  ResponseEntity.ok(employerProfileService.updateProfile(request,authentication.getName()));
    }
}
