package com.kimhong.job_portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileRequest {

    @NotBlank(message = "Company Name is required")
    private String companyName;

    private String companyDescription; //optional
    private String website; //optional
    private String location; //optional
}
