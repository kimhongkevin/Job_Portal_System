package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.JobStatus;
import com.kimhong.job_portal.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private JobType jobType;
    private BigDecimal salary;
    private JobStatus jobStatus;
    private Long employerId;
    private String companyName;
    private LocalDateTime createdAt;
}
