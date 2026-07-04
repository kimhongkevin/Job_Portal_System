package com.kimhong.job_portal.dto;

import com.kimhong.job_portal.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {
    private ApplicationStatus status; // This is only thing employer can change
}
