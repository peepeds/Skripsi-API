package com.example.skripsi.repositories;

import com.example.skripsi.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByUserUserId(Long userId);
    List<UserProfile> findAllByUser_UserIdIn(List<Long> userIds);
}
