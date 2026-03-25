package com.example.skripsi.models.auth;

import com.example.skripsi.validation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Register {
    @NotBlank
    private String firstName;
    private String LastName;
    @NotBlank
    @ValidEmail
    private String email;
    @NotBlank
    @ValidPassword
    private String password;
    @NotNull
    private Integer regionId;
    @NotNull
    private Integer majorId;
    @NotBlank
    @Size(min = 5, max = 10)
    @ValidRegisterId
    private String registerId;
    @NotBlank
    @ValidPhoneNumber
    private String phoneNumber;
}
