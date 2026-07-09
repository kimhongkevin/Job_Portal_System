package com.kimhong.job_portal.service;

import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.repository.JobApplicationRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import com.kimhong.job_portal.repository.SeekerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private SeekerProfileRepository seekerProfileRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User seekerUser;
    private User employerUser;
    private SeekerProfile mockSeeker;
    private EmployerProfile mockEmployer;
    private JobPosting mockJob;
    private JobApplication mockApplication;

    private final String seekerEmail = "seeker@example.com";
    private final String employerEmail = "employer@example.com";

    @BeforeEach
    void setUp(){
        seekerUser = new User();
        seekerUser.setRole(Role.JOB_SEEKER);
        seekerUser.setEmail(seekerEmail);

        mockSeeker = new SeekerProfile();
        mockSeeker.setId(1L);
        mockSeeker.setUser(seekerUser);

        employerUser = new User();
        employerUser.setRole(Role.EMPLOYER);
        employerUser.setEmail(employerEmail);

        mockEmployer = new EmployerProfile();
        mockEmployer.setId(5L);
        mockEmployer.setCompanyName("Tech Corps");
        mockEmployer.setUser(employerUser);

        mockJob = new JobPosting();
        mockJob.setId(10L);
        mockJob.setTitle("Java Developer");
        mockJob.setEmployer(mockEmployer);
        mockJob.setJobStatus(JobStatus.OPEN);

        mockApplication = JobApplication.builder()
                .id(15L)
                .seeker(mockSeeker)
                .job(mockJob)
                .status(ApplicationStatus.PENDING)
                .coverLetter("Apply for Java Developer")
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should apply successfully when constraints are completely satisfied")
    void applyToJob_Success() {}

    @Test
    @DisplayName("Should throw ResourceNotFoundException when seeker profile does not exist")
    void applyToJob_ThrowsException_WhenSeekerProfileNotFound() {}

    @Test
    @DisplayName("Should throw BadRequestException when trying to apply to a closed job")
    void applyToJob_ThrowsException_WhenJobIsClosed() {}

    @Test
    @DisplayName("Should throw DuplicateResourceException when application already exists")
    void applyToJob_ThrowsException_WhenAlreadyApplied() {}

    @Test
    @DisplayName("Should return seeker applications list successfully")
    void getMyApplication_Success() {}

    @Test
    @DisplayName("Should return application list if requesting user is the employer owner")
    void getApplicationForJob_Success() {}

    @Test
    @DisplayName("Should throw UnauthorizedException when requested by an external user")
    void getApplicationForJob_ThrowsException_WhenUnauthorizedUser() {}

    @Test
    @DisplayName("Should update status successfully when requested by owner employer")
    void updateApplicationStatus_Success() {}

    @Test
    @DisplayName("Should throw UnauthorizedException when an external user changes status")
    void updateApplicationStatus_ThrowsException_WhenUnauthorized() {}

    @Test
    @DisplayName("Should complete deletion when application is still PENDING and user is owner seeker")
    void withdrawApplication_Success() {}

    @Test
    @DisplayName("Should throw UnauthorizedException when another user attempts withdrawal")
    void withdrawApplication_ThrowsException_WhenUnauthorizedUser() {}

    @Test
    @DisplayName("Should throw BadRequestException if application is no longer in PENDING phase")
    void withdrawApplication_ThrowsException_WhenNotPending() {}

    @Test
    @DisplayName("Should return filtered applications matching specific status for authorized employer")
    void getApplicationByStatus_Success() {}


}
