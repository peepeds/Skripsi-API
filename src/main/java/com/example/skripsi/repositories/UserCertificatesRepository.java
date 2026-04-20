package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface UserCertificatesRepository extends JpaRepository<UserCertificates, Long> {
    List<UserCertificates> findByUserProfile_UserProfileId(Long userProfileId);

    @Query("SELECT uc.userProfile.user.userId FROM UserCertificates uc WHERE uc.issuer = :companyId AND uc.userProfile.user.userId IN :userIds")
    Set<Long> findVerifiedUserIdsByIssuerAndUserIds(@Param("companyId") Long companyId, @Param("userIds") List<Long> userIds);
}
