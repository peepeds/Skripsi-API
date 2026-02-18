package com.example.skripsi.services;

import com.example.skripsi.entities.*;
import com.example.skripsi.exceptions.CustomAccesDeniedExceptions;
import com.example.skripsi.exceptions.InvalidCredentialsException;
import com.example.skripsi.interfaces.IUserService;
import com.example.skripsi.models.user.UserResponse;
import com.example.skripsi.repositories.UserProfileRepository;
import com.example.skripsi.repositories.UserRepository;
import com.example.skripsi.securities.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       SecurityUtils securityUtils){
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<UserResponse> getAllUserByUserPrivilege() {

        Long userId = securityUtils.getCurrentUserId();

        var privilegeLevel = userRepository.getUserPrivilege(userId)
                .orElseThrow(() -> new CustomAccesDeniedExceptions("Insufficient privileges"))
                .toLowerCase();

        var users = userRepository.getUserByUserPrivilege(privilegeLevel, userId);
        if (users.isEmpty()) {
            return List.of();
        }

        var userIds = users.stream().map(User::getUserId).toList();
        var userProfiles = userProfileRepository.findAllByUser_UserIdIn(userIds);
        var profileMap = userProfiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getUserId(), // key
                        p -> p, // values
                        (existing, replacement) -> existing
                ));

        return users.stream()
                .map(user -> toResponse(user, profileMap.get(user.getUserId())))
                .toList();
    }

    public Boolean emailExists(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserResponse getUserProfile() {
        Long userId = securityUtils.getCurrentUserId();
        UserProfile userProfile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User profile not found")
                );

        return toUserResponse(userProfile);
    }

    private UserResponse toResponse(User user, UserProfile profile) {

        var roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse("USER")
                .toLowerCase();

        var phoneNumber = Optional.ofNullable(profile)
                .map(UserProfile::getPhoneNumber)
                .orElse(null);

        var major = Optional.ofNullable(profile)
                .map(UserProfile::getMajor)
                .orElse(null);

        var regionName = Optional.ofNullable(major)
                .map(Major::getRegion)
                .map(Region::getRegionName)
                .orElse(null);

        var deptName = Optional.ofNullable(major)
                .map(Major::getDepartment)
                .map(Department::getDeptName)
                .orElse(null);

        var majorName = Optional.ofNullable(major)
                .map(Major::getMajorName)
                .orElse(null);

        return UserResponse.builder()
                .fullName("%s %s".formatted(
                        user.getFirstName(),
                        Optional.ofNullable(user.getLastName()).orElse("").trim()
                ))
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .regionName(regionName)
                .deptName(deptName)
                .majorName(majorName)
                .role(roleName)
                .build();
    }

    private UserResponse toUserResponse(UserProfile userProfile) {
        return UserResponse.builder()
                .fullName("%s %s".formatted(
                        userProfile.getUser().getFirstName(),
                        Optional.ofNullable(userProfile.getUser().getLastName()).orElse("")
                ).trim())
                .firstName(userProfile.getUser().getFirstName())
                .lastName(userProfile.getUser().getLastName())
                .email(userProfile.getUser().getEmail())
                .phoneNumber(userProfile.getPhoneNumber())
                .regionName(userProfile.getRegion().getRegionName())
                .majorName(userProfile.getMajor().getMajorName())
                .deptName(userProfile.getMajor().getDepartment().getDeptName())
                .build();
    }


}
