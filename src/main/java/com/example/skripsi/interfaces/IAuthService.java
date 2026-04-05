package com.example.skripsi.interfaces;

import com.example.skripsi.models.auth.*;

public interface IAuthService {
    void register(Register register);
    AuthResponse login(Login login);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}
