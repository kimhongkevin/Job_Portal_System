package com.kimhong.job_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeekerProfileRequest {
    private String bio;
    private String skills;
    private String experience;
    private String education;
    private String location;
}
