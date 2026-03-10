package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.exceptions.InvalidCredentialsException;
import com.example.skripsi.exceptions.InvalidTokenException;
import com.example.skripsi.interfaces.IAuthService;
import com.example.skripsi.models.auth.AuthResponse;
import com.example.skripsi.models.auth.Login;
import com.example.skripsi.models.auth.Register;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserTokenRepository userTokenRepository;
    private final RegionRepository regionRepository;
    private final MajorRepository majorRepository;
    private final JwtUtils jwtUtils;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserTokenRepository userTokenRepository,
            RegionRepository regionRepository,
            MajorRepository majorRepository,
            JwtUtils jwtUtils
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userTokenRepository = userTokenRepository;
        this.regionRepository = regionRepository;
        this.majorRepository = majorRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void register(Register register) {
        String email = register.getEmail().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestExceptions("Email already used!");
        }

        Region region = regionRepository.findById(register.getRegionId())
                .orElseThrow(() -> new BadRequestExceptions("Region not found"));

        Major major = majorRepository.findById(register.getMajorId())
                .orElseThrow(() -> new BadRequestExceptions("Major not found"));

        if (!major.getRegion().getRegionId().equals(region.getRegionId())) {
            throw new BadRequestExceptions("Major does not belong to the selected region");
        }

        String encodedPassword = BCrypt.hashpw(
                register.getPassword(),
                BCrypt.gensalt()
        );

        User user = User.builder()
                .firstName(register.getFirstName())
                .lastName(register.getLastName())
                .email(email)
                .password(encodedPassword)
                .createdAt(OffsetDateTime.now())
                .build();

        userRepository.save(user);

        String registerId = register.getRegisterId();
        String studentId = null;
        String lectureId = null;

        if (registerId.length() == 10) {
            studentId = registerId;
        } else if (registerId.length() == 5 && registerId.startsWith("D")) {
            lectureId = registerId;
        } else {
            throw new BadRequestExceptions("Invalid Student ID or Lecture ID!");
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .phoneNumber(register.getPhoneNumber())
                .region(region)
                .major(major)
                .studentId(studentId)
                .lectureId(lectureId)
                .createdAt(OffsetDateTime.now())
                .build();

        userProfileRepository.save(profile);
    }

    @Override
    public AuthResponse login(Login login) {
        User user = userRepository.findByEmail(login.getEmail().toLowerCase())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );

        if (!BCrypt.checkpw(login.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getRoleName)
                .toList();

        String accessToken = jwtUtils.generateAccessToken(user.getEmail(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        String jti = jwtUtils.getJti(refreshToken);

        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plusSeconds(jwtUtils.getRefreshTokenExpirationSeconds());

        UserToken token = UserToken.builder()
                .user(user)
                .jti(jti)
                .createdAt(OffsetDateTime.now())
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        userTokenRepository.save(token);

        return toAuthResponse(accessToken, refreshToken);
    }

    private AuthResponse toAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        String jti = jwtUtils.getJti(refreshToken);

        UserToken tokenRecord = userTokenRepository
                .findValidRefreshToken(jti, OffsetDateTime.now())
                .orElseThrow(() ->
                        new InvalidTokenException("Invalid or expired refresh token")
                );

        User user = tokenRecord.getUser();

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getRoleName)
                .toList();

        String newAccessToken = jwtUtils.generateAccessToken(user.getEmail(), roles);

        return toAuthResponse(newAccessToken, null);
    }

    @Override
    public void logout(String refreshToken) {
        String jti = jwtUtils.getJti(refreshToken);

        UserToken tokenRecord = userTokenRepository
                .findValidRefreshToken(jti, OffsetDateTime.now())
                .orElseThrow(() ->
                        new InvalidTokenException("Invalid or expired refresh token")
                );

        tokenRecord.setRevoked(true);
        userTokenRepository.save(tokenRecord);
    }
}
