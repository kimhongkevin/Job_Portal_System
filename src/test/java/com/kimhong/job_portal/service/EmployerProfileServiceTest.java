package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.entity.*;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployerProfileServiceTest {

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private EmployerProfileService employerProfileService;

    private User mockUser;
    private EmployerProfile mockProfile;
    private final String userEmail = "employer@example.com";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(userEmail);
        mockUser.setRole(Role.EMPLOYER);

        mockProfile = EmployerProfile.builder()
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
                .user(mockUser)
                .build();
    }

    // ─── createProfile tests ─────────────────────────────

    @Test
    @DisplayName("Should successfully create employer profile")
    void createProfile_Success() {
        // Arrange
        EmployerProfileRequest request = new EmployerProfileRequest(
                "Tech Corp", "Leading tech company",
                "techcorp.com", "Phnom Penh",
                Industry.IT, CompanySize.MEDIUM,
                "123 Tech Street", 2015,
                "facebook.com/techcorp", "linkedin.com/techcorp"
        );

        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.existsByUser(mockUser)).thenReturn(false);
        when(employerProfileRepository.save(any(EmployerProfile.class)))
                .thenReturn(mockProfile);

        // Act
        EmployerProfileResponse response = employerProfileService
                .createProfile(request, userEmail);

        // Assert
        assertNotNull(response);
        assertEquals("Tech Corp", response.getCompanyName());
        assertEquals(Industry.IT, response.getIndustry());
        assertEquals(CompanySize.MEDIUM, response.getCompanySize());
        assertEquals(2015, response.getFoundedYear());
        verify(employerProfileRepository, times(1)).save(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when profile already exists")
    void createProfile_ThrowsException_WhenProfileExists() {
        // Arrange
        EmployerProfileRequest request = new EmployerProfileRequest(
                "Tech Corp", null, null, null,
                null, null, null, null, null, null
        );
        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.existsByUser(mockUser)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> employerProfileService.createProfile(request, userEmail));
        verify(employerProfileRepository, never()).save(any());
    }

    // ─── uploadCompanyLogo tests ──────────────────────────

    @Test
    @DisplayName("Should successfully upload company logo")
    void uploadCompanyLogo_Success() {
        // Arrange
        MultipartFile mockImage = mock(MultipartFile.class);
        String newLogoUrl = "uploads/images/abc123.png";

        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser))
                .thenReturn(Optional.of(mockProfile));
        when(fileStorageService.storeImage(mockImage)).thenReturn(newLogoUrl);
        when(employerProfileRepository.save(any(EmployerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployerProfileResponse response = employerProfileService
                .uploadCompanyLogo(mockImage, userEmail);

        // Assert
        assertNotNull(response);
        assertEquals(newLogoUrl, response.getCompanyLogoUrl());
        verify(fileStorageService, times(1)).storeImage(mockImage);
        verify(fileStorageService, never()).deleteFile(any()); // no old logo to delete
        verify(employerProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should delete old logo before uploading new one")
    void uploadCompanyLogo_DeletesOldLogo_WhenExists() {
        // Arrange
        String oldLogoUrl = "uploads/images/old-logo.png";
        mockProfile.setCompanyLogoUrl(oldLogoUrl); // profile has existing logo

        MultipartFile mockImage = mock(MultipartFile.class);
        String newLogoUrl = "uploads/images/new-logo.png";

        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser))
                .thenReturn(Optional.of(mockProfile));
        when(fileStorageService.storeImage(mockImage)).thenReturn(newLogoUrl);
        when(employerProfileRepository.save(any(EmployerProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployerProfileResponse response = employerProfileService
                .uploadCompanyLogo(mockImage, userEmail);

        // Assert
        assertNotNull(response);
        assertEquals(newLogoUrl, response.getCompanyLogoUrl());
        verify(fileStorageService, times(1)).deleteFile(oldLogoUrl); // old logo deleted ✅
        verify(fileStorageService, times(1)).storeImage(mockImage);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile not found for logo upload")
    void uploadCompanyLogo_ThrowsException_WhenProfileNotFound() {
        // Arrange
        MultipartFile mockImage = mock(MultipartFile.class);
        when(userService.getUserByEmail(userEmail)).thenReturn(mockUser);
        when(employerProfileRepository.findByUser(mockUser))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> employerProfileService.uploadCompanyLogo(mockImage, userEmail));
        verify(fileStorageService, never()).storeImage(any());
    }

    // ─── getAllCompanies tests ────────────────────────────

    @Test
    @DisplayName("Should return list of all companies")
    void getAllCompanies_Success() {
        // Arrange
        when(employerProfileRepository.findAll())
                .thenReturn(List.of(mockProfile));
        when(jobPostingRepository.countOpenJobsByEmployer(mockProfile))
                .thenReturn(3L);

        // Act
        List<CompanyPublicResponse> responses = employerProfileService
                .getAllCompanies();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Tech Corp", responses.getFirst().getCompanyName());
        assertEquals(3, responses.getFirst().getActiveJobCount());
        verify(employerProfileRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no companies exist")
    void getAllCompanies_ReturnsEmptyList_WhenNoCompanies() {
        // Arrange
        when(employerProfileRepository.findAll()).thenReturn(List.of());

        // Act
        List<CompanyPublicResponse> responses = employerProfileService
                .getAllCompanies();

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    // ─── getCompanyById tests ─────────────────────────────

    @Test
    @DisplayName("Should return company when valid ID provided")
    void getCompanyById_Success() {
        // Arrange
        when(employerProfileRepository.findById(10L))
                .thenReturn(Optional.of(mockProfile));
        when(jobPostingRepository.countOpenJobsByEmployer(mockProfile))
                .thenReturn(5L);

        // Act
        CompanyPublicResponse response = employerProfileService
                .getCompanyById(10L);

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
        when(employerProfileRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> employerProfileService.getCompanyById(999L));
    }

    // ─── searchCompanies tests ────────────────────────────

    @Test
    @DisplayName("Should return companies matching keyword")
    void searchCompanies_Success() {
        // Arrange
        when(employerProfileRepository
                .findByCompanyNameContainingIgnoreCase("tech"))
                .thenReturn(List.of(mockProfile));
        when(jobPostingRepository.countOpenJobsByEmployer(mockProfile))
                .thenReturn(2L);

        // Act
        List<CompanyPublicResponse> responses = employerProfileService
                .searchCompanies("tech");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Tech Corp", responses.getFirst().getCompanyName());
        verify(employerProfileRepository, times(1))
                .findByCompanyNameContainingIgnoreCase("tech");
    }

    @Test
    @DisplayName("Should return empty list when no companies match keyword")
    void searchCompanies_ReturnsEmptyList_WhenNoMatch() {
        // Arrange
        when(employerProfileRepository
                .findByCompanyNameContainingIgnoreCase("xyz"))
                .thenReturn(List.of());

        // Act
        List<CompanyPublicResponse> responses = employerProfileService
                .searchCompanies("xyz");

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }
}
