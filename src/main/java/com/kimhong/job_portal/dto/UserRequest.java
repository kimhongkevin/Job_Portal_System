package com.kimhong.job_portal.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String fullName;
    private String currentPassword;

    @Size(min=8, message = "Password must have at least 8 characters")
    private String newPassword;
}
