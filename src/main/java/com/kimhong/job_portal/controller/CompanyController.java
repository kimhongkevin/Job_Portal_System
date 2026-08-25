package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.CompanyProfileRequest;
import com.kimhong.job_portal.dto.CompanyProfileResponse;
import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.service.CompanyProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Public GETs for everyone + admin-managed company profiles.
// contactEmail on a company is where candidate CV emails are sent.
@RestController
@RequestMapping("/api/companies")
@Tag(name = "Companies", description = "Company profile endpoints (public read, admin write)")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyProfileService companyProfileService;

    // ─── Public endpoints ────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all companies (public)")
    public ResponseEntity<List<CompanyPublicResponse>> getAllCompanies(){
        return ResponseEntity.ok(companyProfileService.getAllCompanies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by id (public)")
    public ResponseEntity<CompanyPublicResponse> getCompanyById(@PathVariable Long id){
        return ResponseEntity.ok(companyProfileService.getCompanyById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies by name (public)")
    public ResponseEntity<List<CompanyPublicResponse>> searchCompanies(@RequestParam String keyword){
        return ResponseEntity.ok(companyProfileService.searchCompanies(keyword));

    }

    // ─── Admin endpoints ─────────────────────────────────

    @PostMapping
    @Operation(summary = "Admin creates a new company profile",
            description = "contactEmail is required — CV emails are sent to this address when seekers apply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyProfileResponse> createCompany(
            @Valid @RequestBody CompanyProfileRequest request){

        return ResponseEntity.ok(companyProfileService.createCompany(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Admin updates a company profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyProfileResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyProfileRequest request){

        return ResponseEntity.ok(companyProfileService.updateCompany(id, request));
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin uploads a company logo")
    public ResponseEntity<CompanyProfileResponse> uploadCompanyLogo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            )
            @RequestParam MultipartFile image,
            @PathVariable Long id
            ){
        return ResponseEntity.ok(companyProfileService.uploadCompanyLogo(id, image));

    }

}
