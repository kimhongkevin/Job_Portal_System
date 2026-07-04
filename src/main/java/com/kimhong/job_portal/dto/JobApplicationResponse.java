package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long seekerId;
    private String seekerEmail;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime appliedAt;
}
