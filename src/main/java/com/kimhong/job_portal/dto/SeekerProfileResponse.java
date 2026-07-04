package com.kimhong.job_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeekerProfileResponse {
    private Long id;
    private String bio;
    private String skills;
    private String experience;
    private String education;
    private String location;
    private String userEmail;
    private String resumeUrl;
    private LocalDateTime createdAt;
}
