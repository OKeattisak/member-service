package com.example.memberservice.filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

    private final String authenticationHeader = "Authorization";
    private final String authenticationScheme = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(authenticationHeader);
        if (token == null || !token.startsWith(authenticationScheme)) {
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring(authenticationScheme.length());
    }

}
