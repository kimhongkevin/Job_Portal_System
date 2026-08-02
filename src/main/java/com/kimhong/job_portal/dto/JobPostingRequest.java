package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.ExperienceLevel;
import com.kimhong.job_portal.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingRequest {
    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private Long categoryId;
    private String requirement;
    private String qualification;
    private String benefits;

    private ExperienceLevel experienceLevel;

    private LocalDate deadline;

    @NotBlank(message = "Job location is required")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private BigDecimal salary; // optional, no validation needed
}
