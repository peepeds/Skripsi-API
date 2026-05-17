package com.example.skripsi.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllUserCursorProjection {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String regionName;
    private String deptName;
    private String majorName;
    private OffsetDateTime registeredAt;
}
