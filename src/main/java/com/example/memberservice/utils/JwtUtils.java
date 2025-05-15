package com.example.memberservice.utils;

import java.sql.Date;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class JwtUtils {
    private final String secretKey = "secretKey";
    private final String issuer = "member-service";
    private final int expiration = 86400;

    public String createAccessToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
            .withSubject(username)
            .withExpiresAt(new Date(System.currentTimeMillis() + expiration * 1000))
            .withIssuer(issuer)
            .sign(algorithm);
    }

    public String createRefreshToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
            .withSubject(username)
            .withExpiresAt(new Date(System.currentTimeMillis() + expiration * 1000))
            .withIssuer(issuer)
            .sign(algorithm);
    }

}
