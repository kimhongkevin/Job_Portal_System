package com.kimhong.job_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name= "job_applications",
    uniqueConstraints = @UniqueConstraint(
            columnNames = {"seeker_id","job_id"}
    )
) // This enforces at the DB level that one seeker only apply to one job once
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seeker_id",nullable = false)
    private SeekerProfile seeker;

    @ManyToOne
    @JoinColumn(name = "job_id",nullable = false)
    private JobPosting job;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @CreationTimestamp
    private LocalDateTime appliedAt;

}
