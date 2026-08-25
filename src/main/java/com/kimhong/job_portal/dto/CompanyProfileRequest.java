package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.CompanySize;
import com.kimhong.job_portal.entity.Industry;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileRequest {

    @NotBlank(message = "Company Name is required")
    private String companyName;

    // Optional
    private String companyDescription;
    private String website;
    private String location;
    private Industry industry;
    private CompanySize companySize;
    private String address;
    private Integer foundedYear;
    private String facebookUrl;
    private String linkedinUrl;

    // HR contact info — critical: CV emails are sent to this address
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    // Name of the HR contact person (used in email greeting)
    private String contactPersonName;
}
