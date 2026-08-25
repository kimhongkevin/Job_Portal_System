package com.kimhong.job_portal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Company profile managed by ADMIN (no linked user account).
 * The contactEmail field is critical — it is where candidate CV emails are sent.
 */
@Entity
@Table(name="company_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company's profile

    @Column(nullable = false)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    private String website;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Industry industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanySize companySize;

    private Integer foundedYear;

    private String companyLogoUrl;

    private String facebookUrl;
    private String linkedinUrl;

    // End of company's profile

    // HR contact info — CV emails are sent here when a seeker applies
    @Column(nullable = false)
    private String contactEmail;

    // Name of the HR contact person, used in email greeting
    private String contactPersonName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
