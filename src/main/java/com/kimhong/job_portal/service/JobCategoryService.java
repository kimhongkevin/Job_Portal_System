package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobCategoryRequest;
import com.kimhong.job_portal.dto.JobCategoryResponse;
import com.kimhong.job_portal.entity.JobCategory;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.JobCategoryRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCategoryService {
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostingRepository jobPostingRepository;

    private JobCategoryResponse mapToJobCategoryResponse(JobCategory request){
        Long jobCount = jobPostingRepository.countOpenJobsByCategory(request);

        return new JobCategoryResponse(
                request.getId(),
                request.getName(),
                request.getDescription(),
                jobCount
        );
    }

    public List<JobCategoryResponse> getAllCategories(){
        return jobCategoryRepository.findAll().stream()
                .map(this::mapToJobCategoryResponse).toList();
    }

    public JobCategoryResponse getCategoryById(Long id){
        return jobCategoryRepository.findById(id)
                .map(this::mapToJobCategoryResponse)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
    }

    public JobCategoryResponse createCategory(JobCategoryRequest request){
        if(jobCategoryRepository.existsByName(request.getName()))
            throw new DuplicateResourceException("Category already exists");
        JobCategory category = JobCategory.builder()
                .name(request.getName())
                .description(request.getDescription()).build();

        return mapToJobCategoryResponse(jobCategoryRepository.save(category));
    }

    public JobCategoryResponse updateCategory(Long id,JobCategoryRequest request){
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
        if(request.getName() != null && !request.getName().isBlank())
            category.setName(request.getName());
        if(request.getDescription() != null && !request.getDescription().isBlank())
            category.setDescription(request.getDescription());

        return mapToJobCategoryResponse(jobCategoryRepository.save(category));
    }

    public void deleteCategory(Long id){
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category not found"));
        jobCategoryRepository.delete(category);
    }
}
