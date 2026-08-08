package com.aryan.tradewise_backend.security;

import com.aryan.tradewise_backend.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKey =
            "TradeWiseDevelopmentSecretKey12345678901234567890";

    private final long expirationTime = 15 * 60 * 1000;

    private final SecretKey signingKey =
            Keys.hmacShaKeyFor(
                    secretKey.getBytes(StandardCharsets.UTF_8)
            );

    public String generateToken(User user) {

        Date now = new Date();

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}