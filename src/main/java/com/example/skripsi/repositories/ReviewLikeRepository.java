package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    List<ReviewLike> findByInternshipHeaderId(Long internshipHeaderId);
    List<ReviewLike> findByUserId(Long userId);
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);
}
