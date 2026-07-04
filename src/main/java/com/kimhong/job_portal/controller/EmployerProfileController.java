package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.service.EmployerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/profile")
@RequiredArgsConstructor
public class EmployerProfileController {
    private final EmployerProfileService employerProfileService;

    @PostMapping
    public ResponseEntity<EmployerProfileResponse> createProfile(
            @RequestBody EmployerProfileRequest request,
            Authentication authentication){

        return ResponseEntity.ok(employerProfileService.createProfile(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getMyProfile(Authentication authentication){

        return ResponseEntity.ok(employerProfileService.getMyProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<EmployerProfileResponse> updateMyProfile(
            @RequestBody EmployerProfileRequest request,
            Authentication authentication){
        return  ResponseEntity.ok(employerProfileService.updateProfile(request,authentication.getName()));
    }
}
