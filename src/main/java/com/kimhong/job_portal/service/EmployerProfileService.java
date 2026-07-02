package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.EmployerProfileRequest;
import com.kimhong.job_portal.dto.EmployerProfileResponse;
import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.repository.EmployerProfileRepository;
import com.kimhong.job_portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {
    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;

    public EmployerProfileResponse createProfile(EmployerProfileRequest request,String email){
        User user = getUserByEmail(email);

        if(employerProfileRepository.existsByUser(user))
            throw new RuntimeException("Profile already exists!");

        EmployerProfile profile = mapToEmployer(request,user);
        EmployerProfile savedProfile = employerProfileRepository.save(profile);

        return mapToEmployerResponse(savedProfile);
    }

    public EmployerProfileResponse getMyProfile(String email){
        User user = getUserByEmail(email);

        return employerProfileRepository.findByUser(user)
                .map(this::mapToEmployerResponse)
                .orElseThrow(()->new RuntimeException("Employer profile not found."));
    }

    public EmployerProfileResponse updateProfile(EmployerProfileRequest request, String email){
        User user = getUserByEmail(email);
        EmployerProfile profile = employerProfileRepository.findByUser(user)
                .orElseThrow(()->new RuntimeException("Employer profile not found."));
        profile.setCompanyName(request.getCompanyName());
        profile.setCompanyDescription(request.getCompanyDescription());
        profile.setWebsite(request.getWebsite());
        profile.setLocation(request.getLocation());

        EmployerProfile updatedProfile = employerProfileRepository.save(profile);

        return mapToEmployerResponse(updatedProfile);
    }


    private User getUserByEmail(String email) throws RuntimeException{
        return userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
    }

    private EmployerProfile mapToEmployer(EmployerProfileRequest request, User user){
        return EmployerProfile.builder()
                .id(user.getId())
                .companyName(request.getCompanyName())
                .companyDescription(request.getCompanyDescription())
                .website(request.getWebsite())
                .location(request.getLocation())
                .user(user)
                .build();

    }

    private EmployerProfileResponse mapToEmployerResponse(EmployerProfile employer){
        return  new EmployerProfileResponse(
                employer.getId(),
                employer.getCompanyName(),
                employer.getCompanyDescription(),
                employer.getWebsite(),
                employer.getLocation(),
                employer.getUser().getEmail(),
                employer.getCreatedAt()
        );
    }


}
