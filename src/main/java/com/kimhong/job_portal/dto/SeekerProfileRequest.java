package com.kimhong.job_portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeekerProfileRequest {

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotBlank(message = "Skills is required")
    private String skills;

    private String experience; //optional
    private String education; //optional
    private String location; //optional
}
