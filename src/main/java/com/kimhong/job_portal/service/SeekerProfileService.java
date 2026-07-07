package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.SeekerProfileRequest;
import com.kimhong.job_portal.dto.SeekerProfileResponse;
import com.kimhong.job_portal.entity.SeekerProfile;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.SeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SeekerProfileService {
    private final SeekerProfileRepository seekerProfileRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    private SeekerProfileResponse mapToSeekerProfileResponse(SeekerProfile profile){
        return new SeekerProfileResponse(
                profile.getId(),
                profile.getBio(),
                profile.getSkills(),
                profile.getExperience(),
                profile.getEducation(),
                profile.getLocation(),
                profile.getUser().getEmail(),
                profile.getResumeUrl(),
                profile.getCreatedAt()
        );
    }

    public SeekerProfileResponse createProfile(SeekerProfileRequest request, String email){
        User user = userService.getUserByEmail(email);
        if(seekerProfileRepository.existsByUser(user))
            throw new DuplicateResourceException("User already has profiles");
        SeekerProfile profile = SeekerProfile.builder()
                .user(user)
                .bio(request.getBio())
                .skills(request.getSkills())
                .experience(request.getExperience())
                .education(request.getEducation())
                .location(request.getLocation())
                .build();

        return mapToSeekerProfileResponse(seekerProfileRepository.save(profile));
    }

    public SeekerProfileResponse getMyProfile(String email){
        User user = userService.getUserByEmail(email);

        return seekerProfileRepository.findByUser(user)
                .map(this::mapToSeekerProfileResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found."));
    }

    public SeekerProfileResponse updateProfile(SeekerProfileRequest request, String email){
        User user = userService.getUserByEmail(email);

        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User's profile not found."));

        if(request.getBio() != null && !request.getBio().isBlank())
            profile.setBio(request.getBio());

        if(request.getEducation() != null && !request.getEducation().isBlank())
            profile.setEducation(request.getEducation());

        if(request.getSkills() != null && !request.getSkills().isBlank())
            profile.setSkills(request.getSkills());

        if(request.getLocation() != null && !request.getLocation().isBlank())
            profile.setLocation(request.getLocation());

        if(request.getExperience() != null && !request.getExperience().isBlank())
            profile.setExperience(request.getExperience());

        return mapToSeekerProfileResponse(seekerProfileRepository.save(profile));
    }

    public SeekerProfileResponse uploadResume(MultipartFile file,String email){
        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(()-> new ResourceNotFoundException("Please create profile first"));

        if(profile.getResumeUrl() != null && !profile.getResumeUrl().isBlank())
            fileStorageService.deleteFile(profile.getResumeUrl());

        String fileUrl = fileStorageService.storeFile(file);

        profile.setResumeUrl(fileUrl);

        return mapToSeekerProfileResponse(seekerProfileRepository.save(profile));
    }




}
