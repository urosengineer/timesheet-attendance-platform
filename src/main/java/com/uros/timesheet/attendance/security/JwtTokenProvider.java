package com.uros.timesheet.attendance.security;

import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationMs;
    private final long jwtRefreshExpirationMs;
    private static final String EXPECTED_ALG = "HS256";

    private final CustomUserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.expiration}") long jwtExpirationMs,
            @Value("${security.jwt.refresh-expiration:1209600000}") long jwtRefreshExpirationMs, // default 14 dana
            CustomUserDetailsService userDetailsService
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs;
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .claim("username", userDetails.getUsername())
                .claim("roles", userDetails.getRoleNames())
                .claim("permissions", userDetails.getPermissionNames())
                .claim("organizationId", userDetails.getOrganizationId().toString())
                .claim("organizationName", userDetails.getOrganizationName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        Claims claims = parseAndValidate(token);
        Object type = claims.get("type");
        return "refresh".equals(type) && claims.getExpiration().after(new Date());
    }

    public CustomUserDetails getUserDetailsFromRefreshToken(String token) {
        Claims claims = parseAndValidate(token);
        UUID userId = UUID.fromString(claims.getSubject());
        return (CustomUserDetails) userDetailsService.loadUserByUsername(userId.toString());
    }

    public UUID getUserIdFromJWT(String token) {
        Claims claims = parseAndValidate(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = parseAndValidate(token);
        return claims.get("username", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            parseAndValidate(authToken);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public long getExpirationFromToken(String token) {
        Claims claims = parseAndValidate(token);
        return claims.getExpiration().getTime();
    }

    private Claims parseAndValidate(String token) {
        JwtParser parser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        Jws<Claims> parsed = parser.parseSignedClaims(token);
        String alg = parsed.getHeader().getAlgorithm();
        if (!EXPECTED_ALG.equals(alg)) {
            throw new JwtException("JWT algorithm is not as expected. Expected: " + EXPECTED_ALG + ", got: " + alg);
        }
        return parsed.getPayload();
    }
}