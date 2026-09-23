package com.groupa.digitalbackendapplication.security;

import com.groupa.digitalbackendapplication.domain.dto.helper.TokenClaims;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenService {

    @Value("${jwt.secret.string}")
    private String JWT_SECRET;

    @Value("${jwt.expiration.time}")
    private long EXPIRATION_TIME;


    public String generateToken(String email, Role role, UUID userId, String activeSessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", String.valueOf(role));
        claims.put("tenancyID", String.valueOf(userId));
        claims.put("activeSessionId", activeSessionId);
        return createToken(claims, email);
    }

    public String generateRefreshToken(){
        return UUID.randomUUID().toString();
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    private Boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }


    public TokenClaims extractTokenClaims(String token) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        String sessionId = claims.get("activeSessionId").toString();
        String role = (String)claims.get("role");
        Role userRole = Role.valueOf(role);
        String tenancyId = claims.get("tenancyID").toString();

        return TokenClaims.builder()
                .userSessionId(sessionId)
                .role(userRole)
                .username(username)
                .tenancyId(tenancyId)
                .build();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String createToken(Map<String, Object> claims, String email) {
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey())
                .compact();
    }
}
