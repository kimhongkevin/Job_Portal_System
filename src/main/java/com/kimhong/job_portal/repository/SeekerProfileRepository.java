package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.SeekerProfile;
import com.kimhong.job_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfile,Long> {
    Optional<SeekerProfile> findByUser(User user);
    boolean existsByUser(User user);
    List<SeekerProfile> findByInTalentPoolTrue();
}
