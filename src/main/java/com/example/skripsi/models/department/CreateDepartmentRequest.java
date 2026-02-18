package com.example.skripsi.models.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateDepartmentRequest {

    @NotBlank
    @Size(min = 3)
    private String deptName;

}
