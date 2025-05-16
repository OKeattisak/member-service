package com.example.memberservice.utils;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String secretKey = "yourSuperSecretKey123!@#$";
    private final String issuer = "member-service";
    private final int accessTokenExpiration = 86400;
    private final int refreshTokenExpiration = 604800;

    public String createAccessToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
            .withSubject(username)
            .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000L))
            .withIssuer(issuer)
            .withClaim("type", "ACCESS")
            .sign(algorithm);
    }

    public String createRefreshToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
            .withSubject(username)
            .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000L))
            .withIssuer(issuer)
            .withClaim("type", "REFRESH")
            .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.info("Token validated successfully for subject: {}", decodedJWT.getSubject());
            return decodedJWT;
        } catch (JWTVerificationException exception) {
            logger.error("JWT Validation failed: {}", exception.getMessage());
            return null;
        } catch (IllegalArgumentException exception) {
            logger.error("JWT Validation failed due to illegal argument (e.g. null token): {}", exception.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(DecodedJWT decodedJWT) {
        if (decodedJWT != null) {
            return decodedJWT.getSubject();
        }
        return null;
    }

    public String getClaimFromToken(DecodedJWT decodedJWT, String claimName) {
        if (decodedJWT != null && decodedJWT.getClaim(claimName) != null) {
            return decodedJWT.getClaim(claimName).asString();
        }
        return null;
    }
}
