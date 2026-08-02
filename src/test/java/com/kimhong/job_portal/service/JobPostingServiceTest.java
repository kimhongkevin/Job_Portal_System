package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.PageResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.exception.UnauthorizedException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobCategoryRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JobPostingService jobPostingService;

    private User mockUser;
    private EmployerProfile mockEmployer;
    private JobPosting mockJob;
    private JobCategory mockCategory;
    private final String userEmail = "employer@example.com";

    @BeforeEach
    void setUp(){
        mockUser = new User();
        mockUser.setEmail(userEmail);

        mockEmployer = new EmployerProfile();
        mockEmployer.setId(10L);
        mockEmployer.setCompanyName("Tech Corp");
        mockEmployer.setUser(mockUser);

        mockJob = JobPosting.builder()
                .id(100L)
                .title("Java Developer")
                .description("Looking for a java dev")
                .requirement("skills in Web Development")
                .qualification("Bachelor Degree in CS")
                .benefits("KPI,Bonus,Annual leave")
                .experienceLevel(ExperienceLevel.ENTRY)
                .deadline(null)
                .location("Phnom Penh")
                .jobType(JobType.FULL_TIME)
                .salary(BigDecimal.valueOf(400))
                .jobStatus(JobStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .employer(mockEmployer).build();

        mockCategory = JobCategory.builder()
                .id(11L)
                .name("IT")
                .description("Web Dev,Network,Data Science")
                .createdAt(LocalDateTime.now())
                .updatedAt(null).build();
    }

    // ---- createJob() method testing ----
    @Test
    @DisplayName("Should successfully create a job posting when profile exists")
    void createJob_Success() {
        //Arrange
        JobPostingRequest request = new JobPostingRequest(
                "Java Developer",
                "Looking for a java dev",
                11L,
                "skills in Web Development",
                "Bachelor Degree in CS",
                "KPI,Bonus,Annual leave",
                ExperienceLevel.ENTRY,
                null,
                "Phnom Penh",
                JobType.FULL_TIME,
                BigDecimal.valueOf(500)
        );

        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockEmployer));
        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(mockJob);
        when(jobCategoryRepository.findById(request.getCategoryId())).thenReturn(Optional.ofNullable(mockCategory));

        // Act
        JobPostingResponse response = jobPostingService.createJob(request,userEmail);

        // Assert
        assertNotNull(response);
        assertEquals(mockJob.getId(),response.getId());
        assertEquals(mockJob.getTitle(),response.getTitle());
        assertEquals(mockJob.getEmployer().getId(),response.getEmployerId());

        verify(jobPostingRepository,times(1)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employer profile does not exist")
    void createJob_ThrowsException_WhenProfileNotFound() {
        //Arrange
        JobPostingRequest request = new JobPostingRequest(
                "Java Developer",
                "Looking for a java dev",
                11L,
                "skills in Web Development",
                "Bachelor Degree in CS",
                "KPI,Bonus,Annual leave",
                ExperienceLevel.ENTRY,
                null,
                "Phnom Penh",
                JobType.FULL_TIME,
                BigDecimal.valueOf(500)
        );

        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class,()-> jobPostingService.createJob(request,userEmail));
        verify(jobPostingRepository,never()).save(any(JobPosting.class));

    }

    // ---- getMyJobPosting() method testing ----

    @Test
    @DisplayName("Should return a list of job postings when employer profile exists")
    void getMyJobPosting_Success() {
        // Arrange
        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser)).thenReturn(Optional.of(mockEmployer));
        when(jobPostingRepository.findByEmployer(mockEmployer)).thenReturn(List.of(mockJob));

        // Act
        List<JobPostingResponse> responses = jobPostingService.getMyJobPosting(userEmail);

        // Assert
        assertNotNull(responses);
        assertEquals(1,responses.size());
        assertEquals(mockJob.getId(),responses.getFirst().getId());
        assertEquals(mockJob.getTitle(),responses.getFirst().getTitle());

        // check that our repositories were called exactly one
        verify(userService,times(1)).getUserByEmail(userEmail);
        verify(employerProfileRepository,times(1)).findByUser(mockUser);
        verify(jobPostingRepository,times(1)).findByEmployer(mockEmployer);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employer profile does not exist")
    void getMyJobPosting_ThrowsException_WhenProfileNotFound() {
        // Arrange
        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, ()-> jobPostingService.getMyJobPosting(userEmail));
        verify(jobPostingRepository,never()).findByEmployer(any(EmployerProfile.class));
    }

    // ---- getJobById_Success() method testing ----
    @Test
    @DisplayName("Should return job response when valid ID is provided")
    void getJobById_Success() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        // Act
        JobPostingResponse response = jobPostingService.getJobById(100L);
        // Assert
        assertNotNull(response);
        assertEquals(100L,response.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when job ID does not exist")
    void getJobById_ThrowsException_WhenNotFound(){
        // Arrange
        when(jobPostingRepository.findById(999L)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, ()->jobPostingService.getJobById(999L));
    }


    @Test
    @DisplayName("Should successfully update job fields when requested by the owner")
    void updateJob_Success() {
        // Arrange
        JobPostingRequest updateRequest = new JobPostingRequest(
                "C# Developer",
                "Looking for C# dev",
                11L,
                "skills in Web Development",
                "Bachelor Degree in CS",
                "KPI,Bonus,Annual leave",
                ExperienceLevel.ENTRY,
                null,
                "Siemreap",
                JobType.CONTRACT,
                BigDecimal.valueOf(600)
        );

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobCategoryRepository.findById(11L)).thenReturn(Optional.ofNullable(mockCategory));
        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        JobPostingResponse response = jobPostingService.updateJob(100L,updateRequest,userEmail);

        // Assert
        assertNotNull(response);
        assertEquals("C# Developer",response.getTitle());
        assertEquals("Looking for C# dev",response.getDescription());
        assertEquals("Siemreap",response.getLocation());
        assertEquals(JobType.CONTRACT,response.getJobType());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when an unauthorized user attempts updating")
    void updateJob_ThrowsException_WhenUnauthorized() {
        // Arrange
        JobPostingRequest updateRequest = new JobPostingRequest(
                "Hacker Title",
                "Desc",
                111L,
                "requirements",
                "qualification",
                "benefits",
                ExperienceLevel.ENTRY,
                null,
                "Loc",
                JobType.FULL_TIME,
                BigDecimal.valueOf(500)
        );

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertThrows(UnauthorizedException.class, ()-> jobPostingService.updateJob(100L,updateRequest,"hack@example.com"));
        verify(jobPostingRepository,never()).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("Should delete job successfully if authorized")
    void deleteJob_Success() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertDoesNotThrow(()-> jobPostingService.deleteJob(100L,userEmail));
        verify(jobPostingRepository,times(1)).delete(mockJob);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when deleting someone else's job")
    void deleteJob_ThrowsException_WhenUnauthorized() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        // Act & Assert
        assertThrows(UnauthorizedException.class, ()->jobPostingService.deleteJob(100L,"hack@example.com"));
        verify(jobPostingRepository, never()).delete(mockJob);
    }

    @Test
    @DisplayName("Should return paginated open jobs")
    void getAllOpenJobsPaginated_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0,10);
        Page<JobPosting>  jobPostingPage = new PageImpl<>(List.of(mockJob));
        when(jobPostingRepository.findByJobStatus(JobStatus.OPEN,pageable)).thenReturn(jobPostingPage);
        // Act
        PageResponse<JobPostingResponse> response = jobPostingService.getAllOpenJobsPaginated(pageable);
        // Assert
        assertNotNull(response);
        verify(jobPostingRepository,times(1)).findByJobStatus(JobStatus.OPEN,pageable);

    }

    @Test
    @DisplayName("Should call repository search filter correctly")
    void searchJobs_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0,10);
        Page<JobPosting> emptyPage= new PageImpl<>(List.of(mockJob));
        when(jobCategoryRepository.findById(11L)).thenReturn(Optional.of(mockCategory));
        when(jobPostingRepository.searchJobs("Java","Phnom Penh",JobType.FULL_TIME,mockCategory,ExperienceLevel.ENTRY,pageable)).thenReturn(emptyPage);
        // Act
        PageResponse<JobPostingResponse> response = jobPostingService.searchJobs("Java","Phnom Penh",JobType.FULL_TIME,11L,ExperienceLevel.ENTRY,pageable);
        // Assert
        assertNotNull(response);
        verify(jobCategoryRepository, times(1)).findById(11L);
        verify(jobPostingRepository,times(1)).searchJobs("Java","Phnom Penh",JobType.FULL_TIME,mockCategory,ExperienceLevel.ENTRY,pageable);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID does not exist")
    void searchJobs_ThrowsException_WhenCategoryNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(jobCategoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> jobPostingService.searchJobs(
                        "Java", "Phnom Penh", JobType.FULL_TIME,
                        999L, ExperienceLevel.ENTRY, pageable));

        // Repository search should never be called
        verify(jobPostingRepository, never()).searchJobs(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should change Job Status to CLOSED when requested by authorized employer")
    void closeJob_Success() {
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        JobPostingResponse response = jobPostingService.closeJob(100L,userEmail);

        assertNotNull(response);
        assertEquals(JobStatus.CLOSED,response.getJobStatus());
        verify(jobPostingRepository,times(1)).save(mockJob);
    }

}
