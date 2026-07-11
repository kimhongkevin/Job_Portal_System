package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobApplicationRequest;
import com.kimhong.job_portal.dto.JobApplicationResponse;
import com.kimhong.job_portal.dto.UpdateApplicationStatusRequest;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.exception.UnauthorizedException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private User employerUser;
    private SeekerProfile mockSeekerProfile;
    private EmployerProfile mockEmployerProfile;
    private JobPosting mockJob;
    private JobApplication mockApplication;

    private final String seekerEmail = "seeker@example.com";
    private final String employerEmail = "employer@company.com";

    @BeforeEach
    void setUp() {
        seekerUser = new User();
        seekerUser.setFullName("Jane Doe");
        seekerUser.setEmail(seekerEmail);

        mockSeekerProfile = new SeekerProfile();
        mockSeekerProfile.setId(1L);
        mockSeekerProfile.setUser(seekerUser);

        employerUser = new User();
        employerUser.setEmail(employerEmail);

        mockEmployerProfile = new EmployerProfile();
        mockEmployerProfile.setId(5L);
        mockEmployerProfile.setCompanyName("Tech Corp");
        mockEmployerProfile.setUser(employerUser);

        mockJob = JobPosting.builder()
                .id(100L)
                .title("Backend Engineer")
                .jobStatus(JobStatus.OPEN)
                .employer(mockEmployerProfile)
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

    @Nested
    @DisplayName("Apply To Job Tests")
    class ApplyToJobTests {

        @Test
        @DisplayName("Should apply successfully and send confirmation email")
        void applyToJob_Success() {
            // Arrange
            JobApplicationRequest request = new JobApplicationRequest(100L, "Here is my pitch");
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
            when(jobApplicationRepository.existsBySeekerAndJob(mockSeekerProfile, mockJob)).thenReturn(false);
            when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(mockApplication);

            // Act
            JobApplicationResponse response = jobApplicationService.applyToJob(request, seekerEmail);

            // Assert
            assertNotNull(response);
            verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
            // Verify application email matches parameters completely
            verify(emailService, times(1)).sendApplicationConfirmation(
                    seekerEmail, "Jane Doe", "Backend Engineer", "Tech Corp"
            );
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException and avoid email dispatch when seeker missing")
        void applyToJob_ThrowsException_WhenSeekerProfileNotFound() {
            JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.applyToJob(request, seekerEmail));
            verify(emailService, never()).sendApplicationConfirmation(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw BadRequestException if job status is CLOSED")
        void applyToJob_ThrowsException_WhenJobIsClosed() {
            JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
            mockJob.setJobStatus(JobStatus.CLOSED);

            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

            assertThrows(BadRequestException.class, () -> jobApplicationService.applyToJob(request, seekerEmail));
            verify(emailService, never()).sendApplicationConfirmation(anyString(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should save status and dispatch application update notice email")
        void updateApplicationStatus_Success() {
            // Arrange
            UpdateApplicationStatusRequest statusRequest = new UpdateApplicationStatusRequest(ApplicationStatus.ACCEPTED);
            when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));
            when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            JobApplicationResponse response = jobApplicationService.updateApplicationStatus(500L, statusRequest, employerEmail);

            // Assert
            assertNotNull(response);
            assertEquals(ApplicationStatus.ACCEPTED, response.getStatus());
            verify(emailService, times(1)).sendApplicationStatusUpdate(
                    seekerEmail, "Jane Doe", "Backend Engineer", ApplicationStatus.ACCEPTED
            );
        }

        @Test
        @DisplayName("Should block un-authorized profile status mutation attempts")
        void updateApplicationStatus_ThrowsException_WhenUnauthorized() {
            UpdateApplicationStatusRequest statusRequest = new UpdateApplicationStatusRequest(ApplicationStatus.REJECTED);
            when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

            assertThrows(UnauthorizedException.class, () -> jobApplicationService.updateApplicationStatus(500L, statusRequest, seekerEmail));
            verify(emailService, never()).sendApplicationStatusUpdate(anyString(), anyString(), anyString(), any(ApplicationStatus.class));
        }
    }

    @Nested
    @DisplayName("Fetch Operations Tests")
    class FetchOperationsTests {

        @Test
        @DisplayName("Should successfully pull applications associated with account profile")
        void getMyApplication_Success() {
            when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
            when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
            when(jobApplicationRepository.findBySeeker(mockSeekerProfile)).thenReturn(List.of(mockApplication));

            List<JobApplicationResponse> responses = jobApplicationService.getMyApplication(seekerEmail);
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
    }
}