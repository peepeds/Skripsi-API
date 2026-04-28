package com.example.skripsi.models.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrendMonthResponse {
    private String month;
    private long reviewCount;
    private long newCompanyCount;
}
