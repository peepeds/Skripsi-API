package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.auth.*;
import com.example.skripsi.models.constant.*;
import com.example.skripsi.repositories.*;
import com.example.skripsi.securities.*;
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
    private final IRegionService regionService;
    private final IMajorService majorService;
    private final JwtUtils jwtUtils;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserTokenRepository userTokenRepository,
            IRegionService regionService,
            IMajorService majorService,
            JwtUtils jwtUtils
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userTokenRepository = userTokenRepository;
        this.regionService = regionService;
        this.majorService = majorService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void register(Register register) {
        String email = register.getEmail().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestExceptions(MessageConstants.Auth.EMAIL_ALREADY_USED);
        }

        Region region = regionService.findRegionById(Long.valueOf(register.getRegionId()));

        Major major = majorService.findMajorById(Long.valueOf(register.getMajorId()));

        if (!major.getRegion().getRegionId().equals(region.getRegionId())) {
            throw new BadRequestExceptions(MessageConstants.Validation.MAJOR_DOES_NOT_BELONG_TO_REGION);
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
            throw new BadRequestExceptions(MessageConstants.Auth.INVALID_STUDENT_ID_OR_LECTURE_ID);
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
                        new InvalidCredentialsException(MessageConstants.Auth.INVALID_EMAIL_OR_PASSWORD)
                );

        if (!BCrypt.checkpw(login.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(MessageConstants.Auth.INVALID_EMAIL_OR_PASSWORD);
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
