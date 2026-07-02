package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingRequest {
    private String title;
    private String description;
    private String location;
    private JobType jobType;
    private BigDecimal salary;
}
