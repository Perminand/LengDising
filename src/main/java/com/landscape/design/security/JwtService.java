package com.landscape.design.security;

import com.landscape.design.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String createToken(UUID userId, String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpirationMs());
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    public UUID parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    private SecretKey signingKey() {
        byte[] key = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(key, 0, padded, 0, Math.min(key.length, 32));
            key = padded;
        }
        return Keys.hmacShaKeyFor(key);
    }
}
