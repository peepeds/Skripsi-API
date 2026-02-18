package com.example.skripsi.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtConfig {
    @Value("${jwt.refresh.secret}")
    private String jwtRefreshSecret;
    @Value("${jwt.access.secret}")
    private String jwtAccessSecret;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;
    @Value("${jwt.access.expiration}")
    private long jwtAccessExpiration;
}
