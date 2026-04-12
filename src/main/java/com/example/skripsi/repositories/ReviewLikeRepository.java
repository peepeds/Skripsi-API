package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.repositories.projections.ReviewLikeCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    List<ReviewLike> findByInternshipHeaderId(Long internshipHeaderId);
    List<ReviewLike> findByUserId(Long userId);
    Optional<ReviewLike> findByUserIdAndInternshipHeaderId(Long userId, Long internshipHeaderId);

    @Query("SELECT rl.internshipHeaderId AS internshipHeaderId, COUNT(rl) AS likeCount " +
           "FROM ReviewLike rl " +
           "WHERE rl.internshipHeaderId IN :headerIds AND rl.isLike = true " +
           "GROUP BY rl.internshipHeaderId")
    List<ReviewLikeCountProjection> countLikesByInternshipHeaderIds(@Param("headerIds") List<Long> headerIds);
}
