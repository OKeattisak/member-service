package com.example.memberservice.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.memberservice.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final String AUTHENTICATION_HEADER = "Authorization";
    private final String AUTHENTICATION_SCHEME = "Bearer ";

    private final JwtUtils jwtUtils;

    public JwtFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHENTICATION_HEADER);
        String jwtToken = null;
        String username = null;
        String tokenType = null;

        if (authHeader == null || !authHeader.startsWith(AUTHENTICATION_SCHEME)) {
            logger.trace("Authorization header is missing or does not start with Bearer scheme. Passing to next filter.");
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(AUTHENTICATION_SCHEME.length());
        DecodedJWT decodedAccessToken = jwtUtils.validateToken(jwtToken);

        if (decodedAccessToken == null) {
            logger.warn("JWT token validation failed for token: {}", jwtToken);
            filterChain.doFilter(request, response);
            return;
        }

        username = jwtUtils.getUsernameFromToken(decodedAccessToken);
        tokenType = jwtUtils.getClaimFromToken(decodedAccessToken, "type");
        if (tokenType == null || !tokenType.equals("ACCESS")) {
            logger.warn("Invalid token type received: {}. Expected ACCESS.", tokenType);
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Valid ACCESS token received for user: {}", username);
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("User '{}' authenticated successfully. Setting SecurityContext.", username);
        }else {
            if (username == null) {
                logger.warn("Username could not be extracted from a valid token. This should not happen.");
            }
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.trace("SecurityContextHolder already contains an authentication for '{}'. Skipping.", SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }

        filterChain.doFilter(request, response);
    }

}
