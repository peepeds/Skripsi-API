package com.example.skripsi.controllers;

import com.example.skripsi.configs.JwtConfig;
import com.example.skripsi.exceptions.BadRequestExceptions;
import com.example.skripsi.interfaces.IAuthService;
import com.example.skripsi.models.WebResponse;
import com.example.skripsi.models.auth.Login;
import com.example.skripsi.models.auth.Register;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@Slf4j
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
                .message("Register success")
                .result("Successfully Created Account")
                .build();
    }

    @PostMapping("/login")
    public WebResponse<?> login(
            @Valid @RequestBody Login login,
            HttpServletResponse response
    ) {
        log.info("process...");
        var result = authService.login(login);

        log.info("create refresh cookies");

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .sameSite("None")
                .maxAge((int) (refreshTokenExpiration / 1000))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return WebResponse.builder()
                .success(true)
                .message("Login success")
                .result(Map.of("accessToken", result.getAccessToken()))
                .build();
    }

    @PostMapping("/refresh")
    public WebResponse<?> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new BadRequestExceptions("Missing refresh token");
        }

        var result = authService.refresh(refreshToken);

        return WebResponse.builder()
                .success(true)
                .message("New access token created")
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

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return WebResponse.builder()
                .success(true)
                .message("Logout success")
                .build();
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
