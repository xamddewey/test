package com.xdw.demobackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private static String jwtSecret;

    @Value("${app.jwt.expiration}")
    private static int jwtExpirationMs;

    /**
     * 生成JWT令牌
     */
    public static String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();
        return generateTokenFromUsername(username);
    }

    /**
     * 根据用户名生成JWT令牌
     */
    public static String generateTokenFromUsername(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     */
    public static String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * 验证JWT令牌
     */
    public static boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取签名密钥
     */
    private static SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}