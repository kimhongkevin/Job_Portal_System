package com.kimhong.job_portal.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileResponse {
    private Long id;
    private String companyName;
    private String companyDescription;
    private String website;
    private String location;
    private String userEmail;
    private LocalDateTime createdAt;
}
