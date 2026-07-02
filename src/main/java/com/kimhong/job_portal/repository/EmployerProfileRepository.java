package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.EmployerProfile;
import com.kimhong.job_portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile,Long> {
    Optional<EmployerProfile> findByUser_Id(Long userId);
    Optional<EmployerProfile> findByUser(User user);
    Boolean existsByUser(User user);
}
