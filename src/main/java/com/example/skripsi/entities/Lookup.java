package com.example.skripsi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lookups")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Lookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lookup_id")
    private Long lookupId;

    @Column(name = "lookup_type", nullable = false, length = 50)
    private String lookupType;

    @Column(name = "lookup_code", nullable = false, length = 50)
    private String lookupCode;

    @Column(name = "lookup_value", nullable = false, length = 255)
    private String lookupValue;

    @Column(name = "lookup_description", length = 255)
    private String lookupDescription;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
