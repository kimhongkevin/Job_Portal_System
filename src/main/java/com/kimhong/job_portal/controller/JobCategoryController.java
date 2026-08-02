package com.kimhong.job_portal.controller;


import com.kimhong.job_portal.dto.JobCategoryRequest;
import com.kimhong.job_portal.dto.JobCategoryResponse;
import com.kimhong.job_portal.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    @GetMapping
    @Operation(summary = "Get all categories (public)")
    public ResponseEntity<List<JobCategoryResponse>> getAllCategories(){
        return ResponseEntity.ok(jobCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID (public)")
    public ResponseEntity<JobCategoryResponse> getCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(jobCategoryService.getCategoryById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new category (Admin only)")
    public ResponseEntity<JobCategoryResponse> createCategory(
            @Valid @RequestBody JobCategoryRequest request
    ){
        return ResponseEntity.ok(jobCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category (Admin only)")
    public ResponseEntity<JobCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody JobCategoryRequest request
    ){
        return ResponseEntity.ok(jobCategoryService.updateCategory(id,request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category (Admin only)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){
        jobCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }



}
