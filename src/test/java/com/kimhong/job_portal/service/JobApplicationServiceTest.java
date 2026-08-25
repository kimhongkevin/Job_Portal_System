package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.JobApplicationRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import com.kimhong.job_portal.repository.SeekerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private SeekerProfileRepository seekerProfileRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User seekerUser;
    private SeekerProfile mockSeekerProfile;
    private CompanyProfile mockCompanyProfile;
    private JobPosting mockJob;
    private JobApplication mockApplication;

    private final String seekerEmail = "seeker@example.com";
    private final String hrEmail = "hr@techcorp.com";

    @BeforeEach
    void setUp() {
        seekerUser = new User();
        seekerUser.setFullName("Jane Doe");
        seekerUser.setEmail(seekerEmail);

        mockSeekerProfile = new SeekerProfile();
        mockSeekerProfile.setId(1L);
        mockSeekerProfile.setUser(seekerUser);
        mockSeekerProfile.setResumeUrl("uploads/resumes/resume.pdf");

        mockCompanyProfile = new CompanyProfile();
        mockCompanyProfile.setId(5L);
        mockCompanyProfile.setCompanyName("Tech Corp");
        mockCompanyProfile.setContactEmail(hrEmail);

        mockJob = JobPosting.builder()
                .id(100L)
                .title("Backend Engineer")
                .jobStatus(JobStatus.OPEN)
                .recruitmentModel(RecruitmentModel.JOB_BOARD)
                .company(mockCompanyProfile)
                .build();

        mockApplication = JobApplication.builder()
                .id(500L)
                .seeker(mockSeekerProfile)
                .job(mockJob)
                .status(ApplicationStatus.PENDING)
                .coverLetter("Here is my pitch")
                .appliedAt(LocalDateTime.now())
                .build();
    }

    private void stubHappyPath() {
        when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
        when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobApplicationRepository.existsBySeekerAndJob(mockSeekerProfile, mockJob)).thenReturn(false);
    }

    @Nested
    @DisplayName("Apply To Job Tests")
    class ApplyToJobTests {

        @Test
        @DisplayName("Should apply, email the CV to company HR, and set status to SENT automatically")
        void applyToJob_SendsCVEmail_Success() {
            // Arrange
            stubHappyPath();
            // Record the status at each save() call — save returns the same
            // mutable instance, so we snapshot the status as it happens
            List<ApplicationStatus> statusAtSave = new ArrayList<>();
            when(jobApplicationRepository.save(any(JobApplication.class)))
                    .thenAnswer(inv -> {
                        JobApplication app = inv.getArgument(0);
                        statusAtSave.add(app.getStatus());
                        return app;
                    });
            when(emailService.sendCVToCompanyHR(
                    eq(hrEmail), isNull(), eq("Jane Doe"), eq(seekerEmail),
                    eq("Backend Engineer"), eq("Tech Corp"),
                    eq("uploads/resumes/resume.pdf"), eq("Here is my pitch")))
                    .thenReturn(true);

            // Act
            JobApplicationResponse response = jobApplicationService.applyToJob(
                    new JobApplicationRequest(100L, "Here is my pitch"), seekerEmail);

            // Assert — status flipped to SENT automatically, no admin involved
            assertNotNull(response);
            assertEquals(ApplicationStatus.SENT, response.getStatus());

            verify(emailService, times(1)).sendCVToCompanyHR(
                    eq(hrEmail), isNull(), eq("Jane Doe"), eq(seekerEmail),
                    eq("Backend Engineer"), eq("Tech Corp"),
                    eq("uploads/resumes/resume.pdf"), eq("Here is my pitch"));

            // Seeker confirmation includes the HR address it was sent to
            verify(emailService, times(1)).sendCVSentConfirmation(
                    seekerEmail, "Jane Doe", "Backend Engineer", "Tech Corp", hrEmail);

            // First save = PENDING, second save = SENT
            verify(jobApplicationRepository, times(2)).save(any(JobApplication.class));
            assertEquals(List.of(ApplicationStatus.PENDING, ApplicationStatus.SENT), statusAtSave);
        }

        @Test
        @DisplayName("Should keep status PENDING when the CV email fails to send")
        void applyToJob_StatusStaysPending_WhenEmailFails() {
            // Arrange
            stubHappyPath();
            when(jobApplicationRepository.save(any(JobApplication.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(emailService.sendCVToCompanyHR(
                    anyString(), any(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), any()))
                    .thenReturn(false);

            // Act
            JobApplicationResponse response = jobApplicationService.applyToJob(
                    new JobApplicationRequest(100L, "Here is my pitch"), seekerEmail);

            // Assert — stays PENDING so it can be retried later
            assertNotNull(response);
            assertEquals(ApplicationStatus.PENDING, response.getStatus());
            verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when seeker has no uploaded resume")
        void applyToJob_ThrowsException_WhenNoResume() {
            // Arrange
            mockSeekerProfile.setResumeUrl(null);
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> jobApplicationService.applyToJob(
                            new JobApplicationRequest(100L, "Pitch"), seekerEmail));
            assertEquals("Please upload your resume before applying", ex.getMessage());

            verify(emailService, never()).sendCVToCompanyHR(
                    anyString(), any(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), any());
            verify(jobApplicationRepository, never()).save(any(JobApplication.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException and avoid email dispatch when seeker missing")
        void applyToJob_ThrowsException_WhenSeekerProfileNotFound() {
            JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> jobApplicationService.applyToJob(request, seekerEmail));
            verify(emailService, never()).sendCVToCompanyHR(
                    anyString(), any(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw BadRequestException if job status is CLOSED")
        void applyToJob_ThrowsException_WhenJobIsClosed() {
            JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
            mockJob.setJobStatus(JobStatus.CLOSED);

            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

            assertThrows(BadRequestException.class,
                    () -> jobApplicationService.applyToJob(request, seekerEmail));
            verify(emailService, never()).sendCVToCompanyHR(
                    anyString(), any(), anyString(), anyString(),
                    anyString(), anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when already applied to this job")
        void applyToJob_ThrowsException_WhenAlreadyApplied() {
            JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");

            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
            when(jobApplicationRepository.existsBySeekerAndJob(mockSeekerProfile, mockJob)).thenReturn(true);

            assertThrows(DuplicateResourceException.class,
                    () -> jobApplicationService.applyToJob(request, seekerEmail));
            verify(jobApplicationRepository, never()).save(any(JobApplication.class));
        }
    }

    @Nested
    @DisplayName("Fetch Operations Tests")
    class FetchOperationsTests {

        @Test
        @DisplayName("Should successfully pull applications associated with account profile")
        void getMyApplications_Success() {
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobApplicationRepository.findBySeeker(mockSeekerProfile)).thenReturn(List.of(mockApplication));

            List<JobApplicationResponse> responses = jobApplicationService.getMyApplications(seekerEmail);
            assertFalse(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("Withdrawal Workflows Tests")
    class WithdrawalWorkflowsTests {

        @Test
        @DisplayName("Should drop application entity reference during premature phase cleanly")
        void withdrawApplication_Success() {
            when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));
            assertDoesNotThrow(() -> jobApplicationService.withdrawApplication(500L, seekerEmail));
            verify(jobApplicationRepository, times(1)).delete(mockApplication);
        }

        @Test
        @DisplayName("Should block withdrawal once the application is no longer PENDING")
        void withdrawApplication_ThrowsException_WhenAlreadySent() {
            mockApplication.setStatus(ApplicationStatus.SENT);
            when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

            assertThrows(BadRequestException.class,
                    () -> jobApplicationService.withdrawApplication(500L, seekerEmail));
            verify(jobApplicationRepository, never()).delete(any(JobApplication.class));
        }
    }
}

