package com.kimhong.job_portal.entity;

import lombok.Getter;

@Getter
public enum CompanySize {
    STARTUP("1-10"),
    SMALL("11-50"),
    MEDIUM("51-200"),
    LARGE("201-500"),
    ENTERPRISE(">500");

    private final String range;

    CompanySize(String range) {
        this.range = range;
    }
}
