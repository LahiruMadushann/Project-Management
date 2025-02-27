package com.project_management.security.jwt;

import com.project_management.exceptions.InvalidJwtAuthenticationException;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000; // 1h

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private String getEncodedSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String username, String role, Long userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("userId", userId);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, getEncodedSecretKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(getEncodedSecretKey()).parseClaimsJws(token).getBody().getSubject();
    }
    public Long getUserId(String token) {
        return Long.valueOf(Jwts.parser().setSigningKey(getEncodedSecretKey()).parseClaimsJws(token).getBody().get("userId").toString());
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getEncodedSecretKey()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

    public String getRole(String token) {
        return Jwts.parser()
                .setSigningKey(getEncodedSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
}