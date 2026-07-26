package com.kimhong.job_portal.controller;

import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.service.EmployerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Companies", description = "Public company profile endpoints")
@RequiredArgsConstructor
public class CompanyController {
    private final EmployerProfileService employerProfileService;

    @GetMapping
    @Operation(summary = "Get all companies (public)")
    public ResponseEntity<List<CompanyPublicResponse>> getAllCompanies(){
        return ResponseEntity.ok(employerProfileService.getAllCompanies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by id (public)")
    public ResponseEntity<CompanyPublicResponse> getCompanyById(@PathVariable Long id){
        return ResponseEntity.ok(employerProfileService.getCompanyById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies by name (public)")
    public ResponseEntity<List<CompanyPublicResponse>> searchCompanies(@RequestParam String keyword){
        return ResponseEntity.ok(employerProfileService.searchCompanies(keyword));

    }

}
