package com.kimhong.job_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long jobCount;
}
