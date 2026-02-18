package com.example.skripsi.interfaces;

import com.example.skripsi.models.auth.AuthResponse;
import com.example.skripsi.models.auth.Login;
import com.example.skripsi.models.auth.Register;
import com.example.skripsi.models.user.UserResponse;

public interface IAuthService {
    void register(Register register);
    AuthResponse login(Login login);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}
