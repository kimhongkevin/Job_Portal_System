package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.ExperienceLevel;
import com.kimhong.job_portal.entity.JobStatus;
import com.kimhong.job_portal.entity.JobType;
import com.kimhong.job_portal.entity.RecruitmentModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingResponse {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String requirement;
    private String qualification;
    private String benefits;
    private ExperienceLevel experienceLevel;
    private LocalDate deadline;
    private String location;
    private JobType jobType;
    private BigDecimal maxSalary;
    private BigDecimal minSalary;
    private String salaryDisplay;
    private JobStatus jobStatus;
    private RecruitmentModel recruitmentModel;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
}
