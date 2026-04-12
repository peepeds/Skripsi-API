package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "company_saves")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompanySave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_save_id")
    private Long companySaveId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "is_save", nullable = false)
    private Boolean isSave;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
