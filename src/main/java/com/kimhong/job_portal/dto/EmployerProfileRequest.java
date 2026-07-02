package com.kimhong.job_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileRequest {

    private String companyName;
    private String companyDescription;
    private String website;
    private String location;
}
