package com.malirussia.transfer.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey cleSecrete;
    private final long dureeExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long dureeExpirationMs
    ) {
        this.cleSecrete = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeExpirationMs = dureeExpirationMs;
    }

    public String genererToken(String username) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + dureeExpirationMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cleSecrete)
                .compact();
    }

    public String extraireUsername(String token) {
        return Jwts.parser()
                .verifyWith(cleSecrete)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean estValide(String token) {
        try {
            Jwts.parser().verifyWith(cleSecrete).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}