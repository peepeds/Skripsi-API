package com.example.skripsi.securities;

import com.example.skripsi.exceptions.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtils jwtUtils;

    private final HandlerExceptionResolver resolver;

    public JwtFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, JwtUtils jwtUtils) {
        this.resolver = resolver;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtils.parseClaims(token);

            Date expiration = claims.getExpiration();

            if (expiration != null && expiration.before(new Date())) {
                throw new InvalidTokenException("Token expired");
            }

            String email = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles", List.class);

            if (email == null || userId == null || roles == null) {
                throw new InvalidTokenException("Invalid token claims");
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                AuthUser authUser = new AuthUser(userId, email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authUser,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired", e);
            resolver.resolveException(
                    request,
                    response,
                    null,
                    new InvalidCredentialsException("Token Expired")
            );
        } catch (Exception e) {
            log.debug("Invalid JWT token", e);
            resolver.resolveException(
                    request,
                    response,
                    null,
                    new InvalidCredentialsException("Invalid token")
            );
        }
    }
}
