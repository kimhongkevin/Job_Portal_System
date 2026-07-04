package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.SeekerProfile;
import com.kimhong.job_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeekerProfileRepository extends JpaRepository<SeekerProfile,Long> {
    Optional<SeekerProfile> findByUser(User user);
    boolean existsByUser(User user);
}
