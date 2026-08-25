package com.kimhong.job_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(columnDefinition = "TEXT")
    private String qualification;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    private LocalDate deadline;

    private String location;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(precision = 10, scale = 2)
    private BigDecimal minSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxSalary;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus = JobStatus.OPEN;

    // JOB_BOARD = public & applyable, TALENT_POOL = hidden from public, admin nominates
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RecruitmentModel recruitmentModel = RecruitmentModel.JOB_BOARD;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private JobCategory category;

    // Company this posting belongs to (managed by admin)
    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyProfile company;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
