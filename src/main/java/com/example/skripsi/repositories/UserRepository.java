package com.example.skripsi.repositories;

import com.example.skripsi.entities.*;
import com.example.skripsi.models.user.AllUserCursorProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUserId(Long userId);
    Optional<User> findByEmail(String email);
    @Query("SELECT r.privilegeLevel FROM User u JOIN u.roles r WHERE u.userId = :userId")
    Optional<String> getUserPrivilege(@Param("userId") Long userId);
    @Query(value = """
        SELECT u.*
        FROM users u
        JOIN user_profiles up ON up.user_id = u.user_id
        JOIN majors m ON m.major_id = up.major_id
        JOIN users cu ON cu.user_id = :userId
        JOIN user_profiles cup ON cup.user_id = cu.user_id
        JOIN majors cm ON cm.major_id = cup.major_id
        WHERE
            CASE
                WHEN :userPrivilege = 'all' THEN TRUE
                WHEN :userPrivilege = 'cross_dept' THEN m.dept_id = cm.dept_id
                WHEN :userPrivilege = 'dept' THEN m.dept_id = cm.dept_id AND m.region_id = cm.region_id
                WHEN :userPrivilege = 'major' THEN
                    m.region_id = cm.region_id
                    AND m.major_id = cm.major_id
                ELSE FALSE
            END
    """, nativeQuery = true)
    List<User> getUserByUserPrivilege(@Param("userPrivilege") String userPrivilege, @Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findByUserIdMap(@Param("userIds") List<Long> userIds);

    @Query("""
        SELECT new com.example.skripsi.models.user.AllUserCursorProjection(
            u.userId,
            u.firstName,
            u.lastName,
            u.email,
            COALESCE(r.roleName, 'USER'),
            COALESCE(mr.regionName, pr.regionName),
            d.deptName,
            m.majorName,
            u.createdAt
        )
        FROM User u
        LEFT JOIN UserProfile up ON up.user = u
        LEFT JOIN up.region pr
        LEFT JOIN up.major m
        LEFT JOIN m.region mr
        LEFT JOIN m.department d
        LEFT JOIN u.roles r
        WHERE (:cursor IS NULL OR u.userId > :cursor)
          AND (
            LOWER(COALESCE(u.firstName, '')) LIKE LOWER(:searchPattern)
            OR LOWER(COALESCE(u.lastName, '')) LIKE LOWER(:searchPattern)
          )
        ORDER BY u.userId ASC
    """)
    List<AllUserCursorProjection> findAllUsersFromCursor(
            @Param("cursor") Long cursor,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );
}
