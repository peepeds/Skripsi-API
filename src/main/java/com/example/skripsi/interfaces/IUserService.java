package com.example.skripsi.interfaces;

import com.example.skripsi.models.user.UserResponse;

import java.util.List;

public interface IUserService {
    public List<UserResponse> getAllUserByUserPrivilege();
    public Boolean emailExists(String email);
    UserResponse getUserProfile();
}
