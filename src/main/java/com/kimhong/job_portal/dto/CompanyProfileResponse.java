package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.CompanySize;
import com.kimhong.job_portal.entity.Industry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileResponse {
    private Long id;
    private String companyName;
    private String companyDescription;
    private String website;
    private String location;

    private Industry industry;
    private CompanySize companySize;
    private String address;
    private Integer foundedYear;
    private String companyLogoUrl;
    private String facebookUrl;
    private String linkedinUrl;

    // HR contact info
    private String contactEmail;
    private String contactPersonName;

    private LocalDateTime createdAt;
}
