package com.example.skripsi.models.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MajorOptionResponse {
    public int regionId;
    public int majorId;
    public String majorName;
}
