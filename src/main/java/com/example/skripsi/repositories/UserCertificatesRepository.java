package com.example.skripsi.repositories;

import com.example.skripsi.entities.UserCertificates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCertificatesRepository extends JpaRepository<UserCertificates, Long> {
    List<UserCertificates> findByUserProfile_UserProfileId(Long userProfileId);
}
