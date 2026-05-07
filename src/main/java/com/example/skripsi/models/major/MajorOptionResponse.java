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
    public Integer id;
    public String name;
    public Integer departmentId;
    public String departmentName;
    public Integer regionId;
    public String regionName;
    public String department;
    public String region;
    public Integer majorId;
    public String majorName;

    public MajorOptionResponse(Integer departmentId,
                               String departmentName,
                               Integer regionId,
                               String regionName,
                               Integer majorId,
                               String majorName) {
        this.id = majorId;
        this.name = majorName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.regionId = regionId;
        this.regionName = regionName;
        this.department = departmentName;
        this.region = regionName;
        this.majorId = majorId;
        this.majorName = majorName;
    }
}
