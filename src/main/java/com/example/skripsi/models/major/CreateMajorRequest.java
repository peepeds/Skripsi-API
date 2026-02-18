package com.example.skripsi.models.major;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMajorRequest {
    @NotBlank
    private String majorName;
    @NotNull
    private Integer deptId;
    @NotNull
    private Integer regionId;
}
