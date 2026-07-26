package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.CompanyPublicResponse;
import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {
    private final EmployerProfileRepository employerProfileRepository;
    private final UserService userService;
    private final JobPostingRepository jobPostingRepository;
    private final FileStorageService fileStorageService;

    private EmployerProfileResponse mapToEmployerResponse(EmployerProfile profile){
        return  new EmployerProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getWebsite(),
                profile.getLocation(),
                profile.getUser().getEmail(),
                profile.getIndustry(),
                profile.getCompanySize(),
                profile.getAddress(),
                profile.getFoundedYear(),
                profile.getCompanyLogoUrl(),
                profile.getFacebookUrl(),
                profile.getLinkedinUrl(),
                profile.getCreatedAt()
        );
    }

    private CompanyPublicResponse mapToCompanyPubicResponse(EmployerProfile profile){

        int activeJobCount = jobPostingRepository.countOpenJobByEmployer(profile);

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
                activeJobCount,
                profile.getCreatedAt()

        );
    }

    public EmployerProfileResponse createProfile(EmployerProfileRequest request,String email){
        User user = userService.getUserByEmail(email);

        if(employerProfileRepository.existsByUser(user))
            throw new DuplicateResourceException("Profile already exists!");

        EmployerProfile profile = EmployerProfile.builder()
                .companyName(request.getCompanyName())
                .companyDescription(request.getCompanyDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .user(user)
                .industry(request.getIndustry())
                .address(request.getAddress())
                .facebookUrl(request.getFacebookUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .foundedYear(request.getFoundedYear())
                .build();

        return mapToEmployerResponse(employerProfileRepository.save(profile));
    }

    public EmployerProfileResponse getMyProfile(String email){
        User user = userService.getUserByEmail(email);

        return employerProfileRepository.findByUser(user)
                .map(this::mapToEmployerResponse)
                .orElseThrow(()->new ResourceNotFoundException("Employer profile not found."));
    }

    public EmployerProfileResponse updateProfile(EmployerProfileRequest request, String email){
        User user = userService.getUserByEmail(email);
        EmployerProfile profile = employerProfileRepository.findByUser(user)
                .orElseThrow(()->new ResourceNotFoundException("Employer profile not found."));
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

        return mapToEmployerResponse(employerProfileRepository.save(profile));
    }

    public EmployerProfileResponse uploadCompanyLogo (MultipartFile image, String email){
        User user = userService.getUserByEmail(email);
        EmployerProfile profile = employerProfileRepository.findByUser(user)
                .orElseThrow(()-> new ResourceNotFoundException("Employer profile not found."));
        if(profile.getCompanyLogoUrl() != null)
            fileStorageService.deleteFile(profile.getCompanyLogoUrl());

        String imageUrl = fileStorageService.storeImage(image);

        profile.setCompanyLogoUrl(imageUrl);

        return mapToEmployerResponse(employerProfileRepository.save(profile));

    }

    public List<CompanyPublicResponse> getAllCompanies(){
        return employerProfileRepository.findAll().stream()
                .map(this::mapToCompanyPubicResponse).toList();
    }

    public CompanyPublicResponse getCompanyById(Long id){
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No company with ID: "+id));

        return mapToCompanyPubicResponse(profile);
    }

    public List<CompanyPublicResponse> searchCompanies(String keyword){
        return employerProfileRepository.findByCompanyNameContainingIgnoreCase(keyword)
                .stream().map(this::mapToCompanyPubicResponse).toList();
    }






}
