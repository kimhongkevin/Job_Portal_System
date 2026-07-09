package com.kimhong.job_portal.service;

import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobPostingService jobPostingService;

    private User mockUser;
    private EmployerProfile mockEmployer;
    private JobPosting mockJob;
    private final String userEmail = "employer@example.com";

    @BeforeEach
    void setUp(){
        mockUser = new User();
        mockUser.setEmail(userEmail);

        mockEmployer = new EmployerProfile();
        mockEmployer.setId(10L);
        mockEmployer.setCompanyName("Tech Corp");

        mockJob = JobPosting.builder()
                .id(100L)
                .title("Java Developer")
                .description("Looking for a java dev")
                .location("Phnom Penh")
                .jobType(JobType.FULL_TIME)
                .salary(BigDecimal.valueOf(400))
                .jobStatus(JobStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .employer(mockEmployer).build();
    }

    // ---- createJob() method testing ----
    @Test
    @DisplayName("Should successfully create a job posting when profile exists")
    void createJob_Success() {

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employer profile does not exist")
    void createJob_ThrowsException_WhenProfileNotFound() {}

    @Test
    @DisplayName("Should return a list of job postings when employer profile exists")
    void getMyJobPosting_Success() {}

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employer profile does not exist")
    void getMyJobPosting_ThrowsException_WhenProfileNotFound() {}

    @Test
    @DisplayName("Should return job response when valid ID is provided")
    void getJobById_Success() {}

    @Test
    @DisplayName("Should successfully update job fields when requested by the owner")
    void updateJob_Success() {}

    @Test
    @DisplayName("Should throw UnauthorizedException when an unauthorized user attempts updating")
    void updateJob_ThrowsException_WhenUnauthorized() {}

    @Test
    @DisplayName("Should delete job successfully if authorized")
    void deleteJob_Success() {}

    @Test
    @DisplayName("Should throw UnauthorizedException when deleting someone else's job")
    void deleteJob_ThrowsException_WhenUnauthorized() {}

    @Test
    @DisplayName("Should return paginated open jobs")
    void getAllOpenJobsPaginated_Success() {}

    @Test
    @DisplayName("Should call repository search filter correctly")
    void searchJobs_Success() {}

    @Test
    @DisplayName("Should change Job Status to CLOSED when requested by authorized employer")
    void closeJob_Success() {}


}
