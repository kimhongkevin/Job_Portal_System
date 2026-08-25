package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.CompanyProfileRequest;
import com.kimhong.job_portal.dto.CompanyProfileResponse;
import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.entity.CompanyProfile;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.CompanyProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Company profiles are fully managed by ADMIN — there is no
// linked user account and no per-user ownership checks.
@Service
@RequiredArgsConstructor
public class CompanyProfileService {
    private final CompanyProfileRepository companyProfileRepository;
    private final JobPostingRepository jobPostingRepository;
    private final FileStorageService fileStorageService;

    private CompanyProfileResponse mapToCompanyResponse(CompanyProfile profile){
        return  new CompanyProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getWebsite(),
                profile.getLocation(),
                profile.getIndustry(),
                profile.getCompanySize(),
                profile.getAddress(),
                profile.getFoundedYear(),
                profile.getCompanyLogoUrl(),
                profile.getFacebookUrl(),
                profile.getLinkedinUrl(),
                profile.getContactEmail(),
                profile.getContactPersonName(),
                profile.getCreatedAt()
        );
    }

    private CompanyPublicResponse mapToCompanyPublicResponse(CompanyProfile profile){

        Long activeJobCount = jobPostingRepository.countOpenJobsByCompany(profile);

        return new CompanyPublicResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getWebsite(),
                profile.getLocation(),
                profile.getIndustry(),
                profile.getCompanySize(),
                profile.getAddress(),
                profile.getFoundedYear(),
                profile.getCompanyLogoUrl(),
                profile.getFacebookUrl(),
                profile.getLinkedinUrl(),
                Math.toIntExact(activeJobCount),
                profile.getCreatedAt()

        );
    }

    public CompanyProfileResponse createCompany(CompanyProfileRequest request){
        CompanyProfile profile = CompanyProfile.builder()
                .companyName(request.getCompanyName())
                .companyDescription(request.getCompanyDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .industry(request.getIndustry())
                .address(request.getAddress())
                .facebookUrl(request.getFacebookUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .foundedYear(request.getFoundedYear())
                // Critical: CV emails are sent to this address when seekers apply
                .contactEmail(request.getContactEmail())
                .contactPersonName(request.getContactPersonName())
                .build();

        return mapToCompanyResponse(companyProfileRepository.save(profile));
    }

    public CompanyProfileResponse updateCompany(Long id, CompanyProfileRequest request){
        CompanyProfile profile = companyProfileRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Company not found."));

        if(request.getCompanyName() != null && !request.getCompanyName().isBlank())
            profile.setCompanyName(request.getCompanyName());

        if(request.getCompanyDescription() != null && !request.getCompanyDescription().isBlank())
            profile.setCompanyDescription(request.getCompanyDescription());

        if(request.getWebsite() != null && !request.getWebsite().isBlank())
            profile.setWebsite(request.getWebsite());

        if(request.getLocation() != null && !request.getLocation().isBlank())
            profile.setLocation(request.getLocation());

        if(request.getIndustry() != null)
            profile.setIndustry(request.getIndustry());

        if(request.getCompanySize() != null)
            profile.setCompanySize(request.getCompanySize());

        if(request.getAddress() != null && !request.getAddress().isBlank())
            profile.setAddress(request.getAddress());

        if(request.getFoundedYear() != null)
            profile.setFoundedYear(request.getFoundedYear());

        if(request.getFacebookUrl() != null && !request.getFacebookUrl().isBlank())
            profile.setFacebookUrl(request.getFacebookUrl());

        if(request.getLinkedinUrl() != null && !request.getLinkedinUrl().isBlank())
            profile.setLinkedinUrl(request.getLinkedinUrl());

        if(request.getContactEmail() != null && !request.getContactEmail().isBlank())
            profile.setContactEmail(request.getContactEmail());

        if(request.getContactPersonName() != null)
            profile.setContactPersonName(request.getContactPersonName());

        return mapToCompanyResponse(companyProfileRepository.save(profile));
    }

    public CompanyProfileResponse uploadCompanyLogo (Long id, MultipartFile image){
        CompanyProfile profile = companyProfileRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Company not found."));
        if(profile.getCompanyLogoUrl() != null)
            fileStorageService.deleteFile(profile.getCompanyLogoUrl());

        String imageUrl = fileStorageService.storeImage(image);

        profile.setCompanyLogoUrl(imageUrl);

        return mapToCompanyResponse(companyProfileRepository.save(profile));

    }

    public List<CompanyPublicResponse> getAllCompanies(){
        return companyProfileRepository.findAll().stream()
                .map(this::mapToCompanyPublicResponse).toList();
    }

    public CompanyPublicResponse getCompanyById(Long id){
        CompanyProfile profile = companyProfileRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No company with ID: "+id));

        return mapToCompanyPublicResponse(profile);
    }

    public List<CompanyPublicResponse> searchCompanies(String keyword){
        return companyProfileRepository.findByCompanyNameContainingIgnoreCase(keyword)
                .stream().map(this::mapToCompanyPublicResponse).toList();
    }
}

