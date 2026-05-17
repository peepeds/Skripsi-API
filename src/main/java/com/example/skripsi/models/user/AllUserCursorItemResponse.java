package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllUserCursorItemResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String regionName;
    private String deptName;
    private String majorName;
    private OffsetDateTime registeredAt;
}
