package com.example.skripsi.controllers;

import com.example.skripsi.configs.*;
import com.example.skripsi.exceptions.*;
import com.example.skripsi.interfaces.*;
import com.example.skripsi.models.*;
import com.example.skripsi.models.auth.*;
import com.example.skripsi.models.constant.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;
    private final Long refreshTokenExpiration;

    public AuthController(
            IAuthService authService,
            JwtConfig jwtConfig
    ) {
        this.authService = authService;
        this.refreshTokenExpiration = jwtConfig.getJwtRefreshExpiration();
    }

    @PostMapping("/register")
    public WebResponse<?> register(@Valid @RequestBody Register register) {
        authService.register(register);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.REGISTER_SUCCESS)
                .result(MessageConstants.Success.SUCCESSFULLY_CREATED_ACCOUNT)
                .build();
    }

    @PostMapping("/login")
    public WebResponse<?> login(
            @Valid @RequestBody Login login,
            HttpServletResponse response
    ) {
        var result = authService.login(login);

        ResponseCookie cookie = ResponseCookie.from(CookieConstants.REFRESH_TOKEN_NAME, result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path(CookieConstants.REFRESH_TOKEN_PATH)
                .sameSite(CookieConstants.SAME_SITE_POLICY)
                .maxAge((int) (refreshTokenExpiration / 1000))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.LOGIN_SUCCESS)
                .result(Map.of("accessToken", result.getAccessToken()))
                .build();
    }

    @PostMapping("/refresh")
    public WebResponse<?> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new BadRequestExceptions(MessageConstants.Auth.MISSING_REFRESH_TOKEN);
        }

        var result = authService.refresh(refreshToken);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.NEW_ACCESS_TOKEN_CREATED)
                .result(Map.of("accessToken", result.getAccessToken()))
                .build();
    }

    @PostMapping("/logout")
    public WebResponse<?> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        Cookie cookie = new Cookie(CookieConstants.REFRESH_TOKEN_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(CookieConstants.REFRESH_TOKEN_PATH);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return WebResponse.builder()
                .success(true)
                .message(MessageConstants.Success.LOGOUT_SUCCESS)
                .build();
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> CookieConstants.REFRESH_TOKEN_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
