package com.project_management.security.utils;

import com.project_management.exceptions.InvalidJwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            String token = (String) authentication.getCredentials();
            return extractUserIdFromToken(token);
        }
        throw new InvalidJwtAuthenticationException("No authentication found");
    }

    private static Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString("secret-key".getBytes()))
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Unable to extract userId from token");
        }
    }
}