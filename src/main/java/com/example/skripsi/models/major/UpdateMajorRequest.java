package com.example.skripsi.models.major;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMajorRequest {
    @Size(min = 5)
    private String majorName;
    private Integer regionId;
    private Integer deptId;
    private Boolean active;
}
