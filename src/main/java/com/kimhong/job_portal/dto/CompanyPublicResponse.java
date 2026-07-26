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
public class CompanyPublicResponse {
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
    private Integer activeJobCount;
    private LocalDateTime memberSince;
}
