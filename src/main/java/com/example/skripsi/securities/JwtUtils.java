package com.example.skripsi.securities;

import com.example.skripsi.configs.*;
import com.example.skripsi.entities.*;
import com.example.skripsi.repositories.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtils {

    private final Long refreshTokenExpiration;
    private final Long accessTokenExpiration;
    private final Key signingRefreshTokenKey;
    private final Key signingAccessTokenKey;

    private final UserRepository userRepository;

    public JwtUtils(JwtConfig jwtConfig, UserRepository userRepository) {
        this.refreshTokenExpiration = jwtConfig.getJwtRefreshExpiration();
        this.accessTokenExpiration = jwtConfig.getJwtAccessExpiration();

        byte[] refreshKeyBytes = Decoders.BASE64.decode(jwtConfig.getJwtRefreshSecret());
        byte[] accessKeyBytes = Decoders.BASE64.decode(jwtConfig.getJwtAccessSecret());

        this.signingRefreshTokenKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.signingAccessTokenKey = Keys.hmacShaKeyFor(accessKeyBytes);

        this.userRepository = userRepository;
    }

    public String generateAccessToken(String email, List<String> roles) {
        Date now = new Date();
        Date expires = new Date(now.getTime() + accessTokenExpiration);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("User Not Found!"));


        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .claim("userId", user.getUserId())
                .setIssuedAt(now)
                .setExpiration(expires)
                .signWith(signingAccessTokenKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expires = new Date(now.getTime() + refreshTokenExpiration);

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expires)
                .setId(jti)
                .signWith(signingRefreshTokenKey, SignatureAlgorithm.HS256)
                .compact();
    }

    protected Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingAccessTokenKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJti(String token){
        return Jwts.parserBuilder()
                .setSigningKey(signingRefreshTokenKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getId();
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpiration / 1000;
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
