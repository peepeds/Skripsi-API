package com.example.skripsi.configs;

public class SecurityConstants {

    public static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/user/check-email",
            "/region/options",
            "/major/options",
            "/company",
            "/company/{companySlug}",
            "/category",
            "/subcategory/**",
            "/lookup/{type}"
    };

    private SecurityConstants() {
    }
}
