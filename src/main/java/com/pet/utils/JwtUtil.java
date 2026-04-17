package com.pet.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.pet.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    
    private final JwtConfig jwtConfig;
    
    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());
        
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }
    
    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("userId").asLong();
        }
        return null;
    }
    
    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("username").asString();
        }
        return null;
    }
    
    public Integer getRoleFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("role").asInt();
        }
        return null;
    }
    
    public boolean isTokenExpired(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getExpiresAt().before(new Date());
        }
        return true;
    }
}
