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

    @InjectMocks
    private JobApplicationService jobApplicationService;

    // Common fixture entities
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
        seekerUser.setEmail(seekerEmail);

        mockSeekerProfile = new SeekerProfile();
        mockSeekerProfile.setId(1L);
        mockSeekerProfile.setUser(seekerUser);

        // Employer setups
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

    @Test
    @DisplayName("Should apply successfully when constraints are completely satisfied")
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
        assertEquals(mockApplication.getId(), response.getId());
        assertEquals(ApplicationStatus.PENDING, response.getStatus());
        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when seeker profile does not exist")
    void applyToJob_ThrowsException_WhenSeekerProfileNotFound() {
        // Arrange
        JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
        when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
        when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.applyToJob(request, seekerEmail));
        verify(jobPostingRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to apply to a closed job")
    void applyToJob_ThrowsException_WhenJobIsClosed() {
        // Arrange
        JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
        mockJob.setJobStatus(JobStatus.CLOSED);

        when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
        when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> jobApplicationService.applyToJob(request, seekerEmail));
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when application already exists")
    void applyToJob_ThrowsException_WhenAlreadyApplied() {
        // Arrange
        JobApplicationRequest request = new JobApplicationRequest(100L, "Pitch");
        when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
        when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobApplicationRepository.existsBySeekerAndJob(mockSeekerProfile, mockJob)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> jobApplicationService.applyToJob(request, seekerEmail));
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should return seeker applications list successfully")
    void getMyApplication_Success() {
        // Arrange
        when(userService.getUserByEmail(seekerEmail)).thenReturn(seekerUser);
        when(seekerProfileRepository.findByUser(seekerUser)).thenReturn(Optional.of(mockSeekerProfile));
        when(jobApplicationRepository.findBySeeker(mockSeekerProfile)).thenReturn(List.of(mockApplication));

        // Act
        List<JobApplicationResponse> responses = jobApplicationService.getMyApplication(seekerEmail);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(mockApplication.getId(), responses.getFirst().getId());
    }

    @Test
    @DisplayName("Should return application list if requesting user is the employer owner")
    void getApplicationForJob_Success() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobApplicationRepository.findByJob(mockJob)).thenReturn(List.of(mockApplication));

        // Act
        List<JobApplicationResponse> responses = jobApplicationService.getApplicationForJob(100L, employerEmail);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when requested by an external user")
    void getApplicationForJob_ThrowsException_WhenUnauthorizedUser() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> jobApplicationService.getApplicationForJob(100L, "attacker@fake.com"));
    }

    @Test
    @DisplayName("Should update status successfully when requested by owner employer")
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
        verify(jobApplicationRepository, times(1)).save(mockApplication);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when an external user changes status")
    void updateApplicationStatus_ThrowsException_WhenUnauthorized() {
        // Arrange
        UpdateApplicationStatusRequest statusRequest = new UpdateApplicationStatusRequest(ApplicationStatus.REJECTED);
        when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> jobApplicationService.updateApplicationStatus(500L, statusRequest, seekerEmail));
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should complete deletion when application is still PENDING and user is owner seeker")
    void withdrawApplication_Success() {
        // Arrange
        when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

        // Act & Assert
        assertDoesNotThrow(() -> jobApplicationService.withdrawApplication(500L, seekerEmail));
        verify(jobApplicationRepository, times(1)).delete(mockApplication);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when another user attempts withdrawal")
    void withdrawApplication_ThrowsException_WhenUnauthorizedUser() {
        // Arrange
        when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> jobApplicationService.withdrawApplication(500L, "hacker@domain.com"));
        verify(jobApplicationRepository, never()).delete(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if application is no longer in PENDING phase")
    void withdrawApplication_ThrowsException_WhenNotPending() {
        // Arrange
        mockApplication.setStatus(ApplicationStatus.REVIEWED);
        when(jobApplicationRepository.findById(500L)).thenReturn(Optional.of(mockApplication));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> jobApplicationService.withdrawApplication(500L, seekerEmail));
        verify(jobApplicationRepository, never()).delete(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should return filtered applications matching specific status for authorized employer")
    void getApplicationByStatus_Success() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobApplicationRepository.findByJobAndStatus(mockJob, ApplicationStatus.PENDING)).thenReturn(List.of(mockApplication));

        // Act
        List<JobApplicationResponse> responses = jobApplicationService.getApplicationByStatus(100L, ApplicationStatus.PENDING, employerEmail);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(jobApplicationRepository, times(1)).findByJobAndStatus(mockJob, ApplicationStatus.PENDING);
    }
}