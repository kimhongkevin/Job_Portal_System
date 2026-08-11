package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.ExperienceLevel;
import com.kimhong.job_portal.entity.JobType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin(value = "0.0",message = "Minimum salary cannot be negative")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0",message = "Maximum salary cannot be negative")
    private BigDecimal maxSalary;

    @AssertTrue(message = "Maximum salary must be greater or equal to minimum salary")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary.compareTo(minSalary) >= 0;
    }
}
