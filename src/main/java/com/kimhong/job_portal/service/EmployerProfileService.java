package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {
    private final EmployerProfileRepository employerProfileRepository;
    private final UserService userService;

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

        return mapToEmployerResponse(employerProfileRepository.save(profile));
    }

    private EmployerProfileResponse mapToEmployerResponse(EmployerProfile profile){
        return  new EmployerProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getWebsite(),
                profile.getLocation(),
                profile.getUser().getEmail(),
                profile.getCreatedAt()
        );
    }


}
