package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.JobPostingRequest;
import com.kimhong.job_portal.dto.JobPostingResponse;
import com.kimhong.job_portal.dto.PageResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.CompanyProfileRepository;
import com.kimhong.job_portal.repository.JobCategoryRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private JobCategoryRepository jobCategoryRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private CompanyProfile mockCompany;
    private JobPosting mockJob;
    private JobCategory mockCategory;

    @BeforeEach
    void setUp(){
        mockCompany = new CompanyProfile();
        mockCompany.setId(10L);
        mockCompany.setCompanyName("Tech Corp");
        mockCompany.setContactEmail("hr@techcorp.com");

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
                .minSalary(BigDecimal.valueOf(400))
                .maxSalary(BigDecimal.valueOf(800))
                .jobStatus(JobStatus.OPEN)
                .recruitmentModel(RecruitmentModel.JOB_BOARD)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .company(mockCompany).build();

        mockCategory = JobCategory.builder()
                .id(11L)
                .name("IT")
                .description("Web Dev,Network,Data Science")
                .createdAt(LocalDateTime.now())
                .updatedAt(null).build();
    }

    // Admin posts directly for a company — no employer profile needed.
    // Constructor order: title, description, companyId, categoryId, requirement,
    // qualification, benefits, experienceLevel, deadline, location, jobType,
    // minSalary, maxSalary, recruitmentModel
    private JobPostingRequest sampleRequest() {
        return new JobPostingRequest(
                "Java Developer",
                "Looking for a java dev",
                10L,
                11L,
                "skills in Web Development",
                "Bachelor Degree in CS",
                "KPI,Bonus,Annual leave",
                ExperienceLevel.ENTRY,
                LocalDate.of(2026,12,31),
                "Phnom Penh",
                JobType.FULL_TIME,
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(800),
                null
        );
    }

    // ---- createJob() method testing ----
    @Test
    @DisplayName("Should successfully create a job posting for an existing company (defaults to JOB_BOARD)")
    void createJob_Success() {
        //Arrange
        JobPostingRequest request = sampleRequest();
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(jobCategoryRepository.findById(request.getCategoryId())).thenReturn(Optional.ofNullable(mockCategory));
        when(jobPostingRepository.save(any(JobPosting.class))).thenReturn(mockJob);

        // Act
        JobPostingResponse response = jobPostingService.createJob(request);

        // Assert — no ownership checks: admin manages everything
        assertNotNull(response);
        assertEquals(mockJob.getId(),response.getId());
        assertEquals(mockJob.getTitle(),response.getTitle());
        assertEquals(mockCompany.getId(),response.getCompanyId());
        assertEquals(mockCompany.getCompanyName(),response.getCompanyName());
        assertEquals(RecruitmentModel.JOB_BOARD,response.getRecruitmentModel());

        ArgumentCaptor<JobPosting> captor = ArgumentCaptor.forClass(JobPosting.class);
        verify(jobPostingRepository,times(1)).save(captor.capture());
        assertEquals(mockCompany, captor.getValue().getCompany());
    }

    @Test
    @DisplayName("Should keep the requested recruitment model when provided")
    void createJob_KeepsTalentPool_WhenRequested() {
        // Arrange
        JobPostingRequest request = sampleRequest();
        request.setRecruitmentModel(RecruitmentModel.TALENT_POOL);
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(jobCategoryRepository.findById(request.getCategoryId())).thenReturn(Optional.ofNullable(mockCategory));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        JobPostingResponse response = jobPostingService.createJob(request);

        // Assert
        assertEquals(RecruitmentModel.TALENT_POOL, response.getRecruitmentModel());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company does not exist")
    void createJob_ThrowsException_WhenCompanyNotFound() {
        //Arrange
        JobPostingRequest request = sampleRequest();
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,()-> jobPostingService.createJob(request));
        verify(jobPostingRepository,never()).save(any(JobPosting.class));

    }

    // ---- getAllJobs() method testing ----

    @Test
    @DisplayName("Admin should see all job postings regardless of owner")
    void getAllJobs_Success() {
        // Arrange
        when(jobPostingRepository.findAll()).thenReturn(List.of(mockJob));

        // Act
        List<JobPostingResponse> responses = jobPostingService.getAllJobs();

        // Assert
        assertNotNull(responses);
        assertEquals(1,responses.size());
        assertEquals(mockJob.getId(),responses.getFirst().getId());
        assertEquals(mockJob.getTitle(),responses.getFirst().getTitle());
        verify(jobPostingRepository,times(1)).findAll();
    }

    // ---- getJobById() method testing ----

    @Test
    @DisplayName("Should return a job by id when provided")
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

    // ---- updateJob() method testing (no ownership checks anymore) ----

    @Test
    @DisplayName("Should successfully update job fields — admin can update any job")
    void updateJob_Success() {
        // Arrange
        JobPostingRequest updateRequest = new JobPostingRequest(
                "C# Developer",
                "Looking for C# dev",
                10L,
                11L,
                "skills in Web Development",
                "Bachelor Degree in CS",
                "KPI,Bonus,Annual leave",
                ExperienceLevel.ENTRY,
                LocalDate.of(2026,8,31),
                "Siemreap",
                JobType.CONTRACT,
                BigDecimal.valueOf(600),
                BigDecimal.valueOf(1000),
                null
        );

        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(jobCategoryRepository.findById(11L)).thenReturn(Optional.ofNullable(mockCategory));
        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        JobPostingResponse response = jobPostingService.updateJob(100L,updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("C# Developer",response.getTitle());
        assertEquals("Looking for C# dev",response.getDescription());
        assertEquals("Siemreap",response.getLocation());
        assertEquals(JobType.CONTRACT,response.getJobType());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a non-existing job")
    void updateJob_ThrowsException_WhenJobNotFound() {
        // Arrange
        when(jobPostingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> jobPostingService.updateJob(999L, sampleRequest()));
        verify(jobPostingRepository,never()).save(any(JobPosting.class));
    }

    // ---- deleteJob() method testing ----

    @Test
    @DisplayName("Should delete job successfully as admin")
    void deleteJob_Success() {
        // Arrange
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertDoesNotThrow(()-> jobPostingService.deleteJob(100L));
        verify(jobPostingRepository,times(1)).delete(mockJob);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting a non-existing job")
    void deleteJob_ThrowsException_WhenJobNotFound() {
        // Arrange
        when(jobPostingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, ()->jobPostingService.deleteJob(999L));
        verify(jobPostingRepository, never()).delete(any(JobPosting.class));
    }

    // ---- pagination + search testing ----

    @Test
    @DisplayName("Should return paginated open JOB_BOARD jobs only")
    void getAllOpenJobsPaginated_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0,10);
        Page<JobPosting>  jobPostingPage = new PageImpl<>(List.of(mockJob));
        when(jobPostingRepository.findByJobStatusAndRecruitmentModel(
                JobStatus.OPEN, RecruitmentModel.JOB_BOARD, pageable)).thenReturn(jobPostingPage);
        // Act
        PageResponse<JobPostingResponse> response = jobPostingService.getAllOpenJobsPaginated(pageable);
        // Assert
        assertNotNull(response);
        verify(jobPostingRepository,times(1))
                .findByJobStatusAndRecruitmentModel(JobStatus.OPEN, RecruitmentModel.JOB_BOARD, pageable);

    }

    @Test
    @DisplayName("Should call repository search filter correctly")
    void searchJobs_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0,10);
        Page<JobPosting> page= new PageImpl<>(List.of(mockJob));
        when(jobCategoryRepository.findById(11L)).thenReturn(Optional.of(mockCategory));
        when(jobPostingRepository.searchJobs("Java","Phnom Penh",JobType.FULL_TIME,mockCategory,ExperienceLevel.ENTRY,null,null,pageable)).thenReturn(page);
        // Act
        PageResponse<JobPostingResponse> response = jobPostingService.searchJobs("Java","Phnom Penh",JobType.FULL_TIME,11L,ExperienceLevel.ENTRY,null,null,pageable);
        // Assert
        assertNotNull(response);
        verify(jobCategoryRepository, times(1)).findById(11L);
        verify(jobPostingRepository,times(1)).searchJobs("Java","Phnom Penh",JobType.FULL_TIME,mockCategory,ExperienceLevel.ENTRY,null,null,pageable);
    }

    @Test
    @DisplayName("Should search without category when categoryId is not provided")
    void searchJobs_AllowsNullCategory() {
        // Arrange
        Pageable pageable = PageRequest.of(0,10);
        Page<JobPosting> page = new PageImpl<>(List.of(mockJob));
        when(jobPostingRepository.searchJobs(isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(pageable))).thenReturn(page);

        // Act
        PageResponse<JobPostingResponse> response =
                jobPostingService.searchJobs(null, null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(response);
        verify(jobCategoryRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when provided category ID does not exist")
    void searchJobs_ThrowsException_WhenCategoryNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(jobCategoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> jobPostingService.searchJobs(
                        "Java", "Phnom Penh", JobType.FULL_TIME,
                        999L, ExperienceLevel.ENTRY,null,null, pageable));

        // Repository search should never be called
        verify(jobPostingRepository, never()).searchJobs(
                any(), any(), any(), any(), any(), any(),any(),any());
    }

    // ---- closeJob() method testing ----

    @Test
    @DisplayName("Should change Job Status to CLOSED when requested by admin")
    void closeJob_Success() {
        when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(mockJob));
        when(jobPostingRepository.save(any(JobPosting.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        JobPostingResponse response = jobPostingService.closeJob(100L);

        assertNotNull(response);
        assertEquals(JobStatus.CLOSED,response.getJobStatus());
        verify(jobPostingRepository,times(1)).save(mockJob);
    }

}


