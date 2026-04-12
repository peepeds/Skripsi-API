package com.example.skripsi.models.company;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveCompanyRequest {
    @NotNull(message = "isSave is required")
    private Boolean isSave;
}
