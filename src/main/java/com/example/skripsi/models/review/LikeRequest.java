package com.example.skripsi.models.review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeRequest {
    @NotNull
    private Long internshipHeaderId;

    @NotNull
    private Boolean isLike;
}
