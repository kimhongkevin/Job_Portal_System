package com.kimhong.job_portal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTalentPoolRequest {
    @NotNull(message = "inTalentPool is required")
    private Boolean inTalentPool; // true = opt in, false = opt out
}
