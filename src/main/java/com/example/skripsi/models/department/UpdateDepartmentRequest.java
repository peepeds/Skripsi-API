package com.example.skripsi.models.department;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDepartmentRequest {
    @Size(min = 3)
    private String deptName;
    private Boolean active;
}
