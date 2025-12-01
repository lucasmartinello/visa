package com.visa.cardapi.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "b29b82a4d482f032e4cdd573a5a8f23f987fca12fa9bda91273cf00259181a92";
    private final Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    public String generate(String user) {
        return Jwts.builder()
                .setSubject(user)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 dia
                .signWith(signingKey)
                .compact();
    }

    public String validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}