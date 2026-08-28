package com.kimhong.job_portal.service;

import com.kimhong.job_portal.dto.SeekerProfileRequest;
import com.kimhong.job_portal.dto.SeekerProfileResponse;
import com.kimhong.job_portal.entity.SeekerProfile;
import com.kimhong.job_portal.entity.User;
import com.kimhong.job_portal.exception.BadRequestException;
import com.kimhong.job_portal.exception.DuplicateResourceException;
import com.kimhong.job_portal.exception.ResourceNotFoundException;
import com.kimhong.job_portal.repository.SeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SeekerProfileService {
    private final SeekerProfileRepository seekerProfileRepository;
    private final UserService userService;
    private final SupabaseStorageService supabaseStorageService;

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
                profile.getInTalentPool(),
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
            supabaseStorageService.deleteFile(profile.getResumeUrl(),"resumes");

        String fileUrl = supabaseStorageService.uploadResume(file);

        profile.setResumeUrl(fileUrl);

        return mapToSeekerProfileResponse(seekerProfileRepository.save(profile));
    }

    // Seeker opts in/out of the admin talent pool
    public SeekerProfileResponse updateTalentPool(Boolean inTalentPool, String email){
        if(inTalentPool == null)
            throw new BadRequestException("inTalentPool is required");

        User user = userService.getUserByEmail(email);
        SeekerProfile profile = seekerProfileRepository.findByUser(user)
                .orElseThrow(()-> new ResourceNotFoundException("User's profile not found."));

        profile.setInTalentPool(inTalentPool);

        return mapToSeekerProfileResponse(seekerProfileRepository.save(profile));
    }

    // Admin talent pool search: students with inTalentPool = true,
    // optionally filtered by skills (comma separated, case-insensitive).
    // available=true additionally requires an uploaded resume.
    public List<SeekerProfileResponse> searchTalentPool(String skills, Boolean available){
        List<SeekerProfile> pool = seekerProfileRepository.findByInTalentPoolTrue();

        List<String> skillKeywords = skills == null || skills.isBlank()
                ? List.of()
                : Arrays.stream(skills.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .toList();

        return pool.stream()
                .filter(profile -> {
                    if(skillKeywords.isEmpty())
                        return true;
                    String candidateSkills = profile.getSkills() == null
                            ? ""
                            : profile.getSkills().toLowerCase(Locale.ROOT);
                    return skillKeywords.stream().anyMatch(candidateSkills::contains);
                })
                .filter(profile -> available == null
                        || !available
                        || (profile.getResumeUrl() != null && !profile.getResumeUrl().isBlank()))
                .map(this::mapToSeekerProfileResponse).toList();
    }




}
