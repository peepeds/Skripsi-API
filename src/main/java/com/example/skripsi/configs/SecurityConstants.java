package com.example.skripsi.configs;

public class SecurityConstants {

    public static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/user/check-email",
            "/region/options",
            "/department/options",
            "/major/**",
            "/company/**",
            "/category/**",
            "/subcategory/**",
            "/lookup/{type}"
    };

    private SecurityConstants() {
    }
}
