package com.example.skripsi.repositories;

import com.example.skripsi.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken,Long> {
    @Query("SELECT t FROM UserToken t WHERE t.jti = :token AND t.revoked = false AND t.expiresAt > :now")
    Optional<UserToken> findValidRefreshToken(@Param("token") String token, @Param("now") OffsetDateTime now);

}
