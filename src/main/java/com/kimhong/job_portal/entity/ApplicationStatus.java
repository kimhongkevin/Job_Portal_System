package com.kimhong.job_portal.entity;

public enum ApplicationStatus {
    // PENDING  -> just applied, CV not sent yet
    // SENT     -> CV successfully emailed to company HR (automatic)
    // REJECTED -> admin removes invalid/spam application (rare, admin cleanup only)
    PENDING,SENT,REJECTED
}
