package com.kimhong.job_portal.repository;

import com.kimhong.job_portal.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile,Long> {
    List<CompanyProfile> findByCompanyNameContainingIgnoreCase(String keyword);
}
