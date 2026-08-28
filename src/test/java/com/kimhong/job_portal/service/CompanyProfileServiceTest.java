package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.CompanyProfileRequest;
import com.kimhong.job_portal.dto.CompanyProfileResponse;
import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.CompanyProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyProfileServiceTest {

    @Mock
    private CompanyProfileRepository companyProfileRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @InjectMocks
    private CompanyProfileService companyProfileService;

    private CompanyProfile mockCompany;

    @BeforeEach
    void setUp() {
        mockCompany = CompanyProfile.builder()
                .id(10L)
                .companyName("Tech Corp")
                .companyDescription("Leading tech company")
                .website("techcorp.com")
                .location("Phnom Penh")
                .industry(Industry.IT)
                .companySize(CompanySize.MEDIUM)
                .address("123 Tech Street")
                .foundedYear(2015)
                .companyLogoUrl(null)
                .facebookUrl("facebook.com/techcorp")
                .linkedinUrl("linkedin.com/techcorp")
                // HR contact info — CV emails are sent here
                .contactEmail("hr@techcorp.com")
                .contactPersonName("Alice HR")
                .build();
    }

    // ─── createCompany tests ─────────────────────────────

    @Test
    @DisplayName("Should successfully create a company profile as admin")
    void createCompany_Success() {
        // Arrange
        CompanyProfileRequest request = new CompanyProfileRequest(
                "Tech Corp", "Leading tech company",
                "techcorp.com", "Phnom Penh",
                Industry.IT, CompanySize.MEDIUM,
                "123 Tech Street", 2015,
                "facebook.com/techcorp", "linkedin.com/techcorp",
                "hr@techcorp.com", "Alice HR"
        );

        when(companyProfileRepository.save(any(CompanyProfile.class)))
                .thenReturn(mockCompany);

        // Act
        CompanyProfileResponse response = companyProfileService.createCompany(request);

        // Assert — no user account is linked anymore
        assertNotNull(response);
        assertEquals("Tech Corp", response.getCompanyName());
        assertEquals("hr@techcorp.com", response.getContactEmail());
        assertEquals("Alice HR", response.getContactPersonName());

        ArgumentCaptor<CompanyProfile> captor = ArgumentCaptor.forClass(CompanyProfile.class);
        verify(companyProfileRepository, times(1)).save(captor.capture());
        assertEquals("hr@techcorp.com", captor.getValue().getContactEmail());
    }

    // ─── updateCompany tests ─────────────────────────────

    @Test
    @DisplayName("Admin should be able to update any company by id")
    void updateCompany_Success() {
        // Arrange
        CompanyProfileRequest request = new CompanyProfileRequest(
                null, null, null, null,
                null, null, null, null, null, null,
                "newhr@techcorp.com", "Bob HR"
        );
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(companyProfileRepository.save(any(CompanyProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompanyProfileResponse response = companyProfileService.updateCompany(10L, request);

        // Assert
        assertNotNull(response);
        assertEquals("Tech Corp", response.getCompanyName()); // unchanged
        assertEquals("newhr@techcorp.com", response.getContactEmail()); // updated
        assertEquals("Bob HR", response.getContactPersonName());
        verify(companyProfileRepository, times(1)).save(any(CompanyProfile.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a missing company")
    void updateCompany_ThrowsException_WhenNotFound() {
        when(companyProfileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> companyProfileService.updateCompany(999L, new CompanyProfileRequest()));
        verify(companyProfileRepository, never()).save(any());
    }

    // ─── uploadCompanyLogo tests ──────────────────────────

    @Test
    @DisplayName("Should successfully upload company logo as admin")
    void uploadCompanyLogo_Success() {
        // Arrange
        MultipartFile mockImage = mock(MultipartFile.class);
        String newLogoUrl = "uploads/images/abc123.png";

        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(supabaseStorageService.uploadLogo(mockImage)).thenReturn(newLogoUrl);
        when(companyProfileRepository.save(any(CompanyProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompanyProfileResponse response = companyProfileService.uploadCompanyLogo(10L, mockImage);

        // Assert
        assertNotNull(response);
        assertEquals(newLogoUrl, response.getCompanyLogoUrl());
        verify(supabaseStorageService, times(1)).uploadLogo(mockImage);
        verify(supabaseStorageService, never()).deleteFile(anyString(),anyString());
        verify(companyProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should delete old logo before uploading new one")
    void uploadCompanyLogo_DeletesOldLogo_WhenExists() {
        // Arrange
        String oldLogoUrl = "uploads/images/old-logo.png";
        mockCompany.setCompanyLogoUrl(oldLogoUrl); // company has existing logo

        MultipartFile mockImage = mock(MultipartFile.class);
        String newLogoUrl = "uploads/images/new-logo.png";

        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(supabaseStorageService.uploadLogo(mockImage)).thenReturn(newLogoUrl);
        when(companyProfileRepository.save(any(CompanyProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompanyProfileResponse response = companyProfileService.uploadCompanyLogo(10L, mockImage);

        // Assert
        assertNotNull(response);
        assertEquals(newLogoUrl, response.getCompanyLogoUrl());
        verify(supabaseStorageService, times(1)).deleteFile(oldLogoUrl,"logos"); // old logo deleted ✅
        verify(supabaseStorageService, times(1)).uploadLogo(mockImage);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company not found for logo upload")
    void uploadCompanyLogo_ThrowsException_WhenCompanyNotFound() {
        // Arrange
        MultipartFile mockImage = mock(MultipartFile.class);
        when(companyProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> companyProfileService.uploadCompanyLogo(999L, mockImage));
        verify(supabaseStorageService, never()).uploadLogo(any());
    }

    // ─── getAllCompanies tests ────────────────────────────

    @Test
    @DisplayName("Should return list of all companies with active job counts")
    void getAllCompanies_Success() {
        // Arrange
        when(companyProfileRepository.findAll()).thenReturn(List.of(mockCompany));
        when(jobPostingRepository.countOpenJobsByCompany(mockCompany)).thenReturn(3L);

        // Act
        List<CompanyPublicResponse> responses = companyProfileService.getAllCompanies();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Tech Corp", responses.getFirst().getCompanyName());
        assertEquals(3, responses.getFirst().getActiveJobCount());
        verify(companyProfileRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no companies exist")
    void getAllCompanies_ReturnsEmptyList_WhenNoCompanies() {
        // Arrange
        when(companyProfileRepository.findAll()).thenReturn(List.of());

        // Act
        List<CompanyPublicResponse> responses = companyProfileService.getAllCompanies();

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    // ─── getCompanyById tests ─────────────────────────────

    @Test
    @DisplayName("Should return company when valid ID provided")
    void getCompanyById_Success() {
        // Arrange
        when(companyProfileRepository.findById(10L)).thenReturn(Optional.of(mockCompany));
        when(jobPostingRepository.countOpenJobsByCompany(mockCompany)).thenReturn(5L);

        // Act
        CompanyPublicResponse response = companyProfileService.getCompanyById(10L);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Tech Corp", response.getCompanyName());
        assertEquals(5, response.getActiveJobCount());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company ID not found")
    void getCompanyById_ThrowsException_WhenNotFound() {
        // Arrange
        when(companyProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> companyProfileService.getCompanyById(999L));
    }

    // ─── searchCompanies tests ────────────────────────────

    @Test
    @DisplayName("Should return companies matching keyword")
    void searchCompanies_Success() {
        // Arrange
        when(companyProfileRepository.findByCompanyNameContainingIgnoreCase("tech"))
                .thenReturn(List.of(mockCompany));
        when(jobPostingRepository.countOpenJobsByCompany(mockCompany)).thenReturn(2L);

        // Act
        List<CompanyPublicResponse> responses = companyProfileService.searchCompanies("tech");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Tech Corp", responses.getFirst().getCompanyName());
        verify(companyProfileRepository, times(1))
                .findByCompanyNameContainingIgnoreCase("tech");
    }

    @Test
    @DisplayName("Should return empty list when no companies match keyword")
    void searchCompanies_ReturnsEmptyList_WhenNoMatch() {
        // Arrange
        when(companyProfileRepository.findByCompanyNameContainingIgnoreCase("xyz"))
                .thenReturn(List.of());

        // Act
        List<CompanyPublicResponse> responses = companyProfileService.searchCompanies("xyz");

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }
}

